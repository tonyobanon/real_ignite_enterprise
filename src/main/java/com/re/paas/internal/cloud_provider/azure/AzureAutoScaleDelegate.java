package com.re.paas.internal.cloud_provider.azure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithCreate;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithOS;
import com.microsoft.azure.management.compute.VirtualMachine.DefinitionStages.WithPublicIPAddress;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.Subnet;
import com.re.paas.internal.app_provisioning.AppProvisioning;
import com.re.paas.internal.base.core.AppUtils;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.cloud_provider.AutoScaleDelegate;
import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.cloud_provider.Tags;
import com.re.paas.internal.clustering.ClusterNode;
import com.re.paas.internal.clustering.classes.ClusterCredentials;
import com.re.paas.internal.clustering.classes.ClusterJoinWorkflow;
import com.re.paas.internal.clustering.classes.InstanceProfile;
import com.re.paas.internal.clustering.classes.JoinWorkflow;
import com.re.paas.internal.clustering.classes.MasterNodeConfig;
import com.re.paas.internal.clustering.classes.OsPlatform;
import com.re.paas.internal.clustering.classes.Patterns;
import com.re.paas.internal.clustering.classes.ServiceProviders;
import com.re.paas.internal.clustering.classes.StaticInstanceTypes;
import com.re.paas.internal.clustering.classes.Utils;
import com.re.paas.internal.clustering.nic_utils.IPAddresses;
import com.re.paas.internal.errors.ClusteringError;
import com.re.paas.internal.utils.RSAKeyPair;

public class AzureAutoScaleDelegate extends AutoScaleDelegate {

	@Override
	public String getInstanceId() {

		String uuid = null;

		OsPlatform os = Utils.getPlatform();

		switch (os) {

		case LINUX:

			try {

				String[] shellArgs = { "sudo dmidecode | grep UUID" };
				Process shellProcess = new ProcessBuilder(shellArgs).start();
				shellProcess.waitFor();
				BufferedReader shellReader = new BufferedReader(new InputStreamReader(shellProcess.getInputStream()));
				uuid = shellReader.readLine().split(Patterns.KVSeperator().pattern())[1];

			} catch (IOException | InterruptedException e) {
				Exceptions.throwRuntime(e);
			}

			break;

		case WINDOWS:

			try {
				
				InputStream in = AppUtils.getBaseClassloader().getResourceAsStream("cloud_providers/azure/AzureVMIDReader.ps1");

				File psFile = File.createTempFile("AzureVMIDReader", ".ps1");
				FileOutputStream out = new FileOutputStream(psFile);

				Utils.copyTo(in, out);

				String[] powershellArgs = { "powershell.exe", "-Noninteractive", "-OutputFormat Text",
						"-File " + psFile.getAbsolutePath() };
				Process powershellProcess = new ProcessBuilder(powershellArgs).start();
				powershellProcess.waitFor();
				BufferedReader powershellReader = new BufferedReader(
						new InputStreamReader(powershellProcess.getInputStream()));
				uuid = powershellReader.readLine();

				break;

			} catch (IOException | InterruptedException e) {
				Exceptions.throwRuntime(e);
			}

		}

		if (uuid == null) {
			return (String) Exceptions
					.throwRuntime(PlatformException.get(ClusteringError.EMPTY_AZURE_VMID_WAS_RETRIEVED));
		}

		// Validate UUID Pattern
		if (!Patterns.azureVmUUID().matcher(uuid).matches()) {
			return (String) Exceptions
					.throwRuntime(PlatformException.get(ClusteringError.INVALID_AZURE_VMID_WAS_RETRIEVED, uuid));
		}

		return uuid.trim();
	}

	@Override
	public InstanceProfile getInstanceProfile() {

		InstanceProfile profile = null;

		try {

			// ** Validate Instance Type Spec
			String vmSizeType = MasterNodeConfig.get(MasterNodeConfig.AZURE_VIRTUAL_MACHINE_SIZE_TYPE);
			if ((!AzureHelper.isValidVMSizeType(vmSizeType)) && (!vmSizeType.equals(StaticInstanceTypes.INHERIT))
					&& (!vmSizeType.equals(StaticInstanceTypes.DYNAMIC))) {

				Exceptions.throwRuntime(
						PlatformException.get(ClusteringError.INSTANCE_TYPE_HAS_INCORRECT_FORMAT, vmSizeType));
			}

			Azure azure = AzureHelper.getAzure();

			VirtualMachine azureVM = azure.virtualMachines().getById(ClusterNode.getCloudUniqueId());

			// ** Validate Region
			String region = MasterNodeConfig.getOrNull(MasterNodeConfig.REGION);
			if (region != null) {
				if (!AzureHelper.isValidRegion(region)) {
					Exceptions.throwRuntime(PlatformException.get(ClusteringError.REGION_HAS_INCORRECT_FORMAT, region));
				}
			} else {
				region = azureVM.regionName();
			}

			PublicIPAddress publicIp = azureVM.getPrimaryPublicIPAddress();

			// Validate public ip
			if (publicIp == null) {
				Exceptions.throwRuntime(PlatformException.get(ClusteringError.MASTER_DOES_NOT_HAVE_ROUTEABLE_ADDRESS,
						ClusterNode.getWKA().getHostAddress()));

			}

			String publicDnsName = publicIp.leafDomainLabel();

			// Validate public dns name
			if (publicDnsName == null) {
				Exceptions.throwRuntime(PlatformException.get(ClusteringError.MASTER_DOES_NOT_HAVE_PUBLIC_DNS_NAME));
			}

			publicDnsName = publicDnsName + "." + azureVM.regionName() + "." + AzureHelper.dnsNameSuffix();

			NetworkInterface azureNIC = azureVM.getPrimaryNetworkInterface();
			NicIPConfiguration azureIpConfig = azureNIC.primaryIPConfiguration();
			Network network = azureIpConfig.getNetwork();

			// Validate that the primary network interface is connected to a
			// virtual network
			if (network == null) {
				Exceptions.throwRuntime(PlatformException.get(ClusteringError.MASTER_NOT_IN_VIRTUAL_PRIVATE_NETWORK,
						ClusterNode.getWKA().getHostAddress()));
			}

			String vnetId = network.id();
			String securityGroup = azureNIC.networkSecurityGroupId();
			String masterSubnetId = azureIpConfig.inner().subnet().id();
			String masterSubnetCIDR = azureIpConfig.inner().subnet().addressPrefix();

			// ** Validate private subnet id
			String slaveSubnetId = MasterNodeConfig.get(MasterNodeConfig.PRIVATE_SUBNET_ID);

			String slaveSubnetCIDR = null;

			for (Subnet subnet : network.subnets().values()) {
				if (subnet.inner().id().equals(slaveSubnetId)) {
					slaveSubnetCIDR = subnet.inner().addressPrefix();
				}
			}

			if (slaveSubnetCIDR == null) {
				Exceptions.throwRuntime(PlatformException.get(ClusteringError.SUBNET_NOT_FOUND_ON_NETWORK,
						slaveSubnetId, network.id()));
			}

			// Build Instance Profile

			profile = new InstanceProfile();

			profile.withImage(azureVM.osUnmanagedDiskVhdUri()).withDefaultInstanceTypeSpec(azureVM.size().toString())
					.withInstanceTypeSpec(vmSizeType).withDefaultRegion(azureVM.regionName()).withRegion(region)
					.withVirtualNetworkId(vnetId).withMasterSubnetId(masterSubnetId)
					.withMasterSubnetCIDR(masterSubnetCIDR).withSlaveSubnetId(slaveSubnetId)
					.withSlaveSubnetCIDR(slaveSubnetCIDR).withMasterPublicIp(publicIp.ipAddress())
					.withOsPlatform(Utils.getPlatform()).withSecurityGroup(securityGroup)
					.withMasterPublicDNSName(publicDnsName);

		} catch (CloudException e) {
			if (e instanceof CloudException) {

				Exceptions.throwRuntime(PlatformException.get(ClusteringError.ERROR_OCCURED_WHILE_MAKING_SERVICE_CALL,
						AzureHelper.name(), e.getMessage()));
			} else {
				Exceptions.throwRuntime(e);
			}
		}

		return profile;
	}

	@Override
	public void startVM(InstanceProfile iProfile, ClusterCredentials auth, ClusterJoinWorkflow joinWorkflow) {

		// Generate clusterNodeId
		String clusterNodeId = Utils.newSecureRandom();

		// Note we must configure its networking components properly, since this
		// is critical for cluster authentication, later on

		String privateIp;
		Boolean associatePublicIp;
		String subnetId;

		if (joinWorkflow.getWorkflow().equals(JoinWorkflow.REPLICA_MASTER)) {

			privateIp = IPAddresses.generateAddress(iProfile.getMasterSubnetCIDR()).getHostAddress();

			associatePublicIp = true;
			subnetId = iProfile.getMasterSubnetId();

		} else {

			// Randomly assign a private ip in the slave subnet.
			privateIp = IPAddresses.generateAddress(iProfile.getSlaveSubnetCIDR()).getHostAddress();

			associatePublicIp = true;
			subnetId = iProfile.getSlaveSubnetId();
		}

		String instanceId = null;

		// Generate credentials
		AzureVMCredential credentials = new AzureVMCredential(Utils.newSecureRandom(), Utils.newSecureRandom());

		String resourceName = ClusterNode.getResourceName(joinWorkflow);

		try {

			// In Azure, security-groups are applied subnet-level, it cannot
			// be applied to all new cluster instances created.

			Azure azure = AzureHelper.getAzure();

			// Just get Network model
			Network vnet = azure.networks().getById(iProfile.getVirtualNetworkId());

			WithPublicIPAddress withPublicIpAddress = azure.virtualMachines().define(resourceName)
					.withRegion(iProfile.getRegion()).withExistingResourceGroup(AzureHelper.getResourceGroup())
					.withExistingPrimaryNetwork(vnet).withSubnet(subnetId).withPrimaryPrivateIPAddressStatic(privateIp);

			WithOS withOS;

			// Branch -> associatePublicIp
			// Remember: Unlike AWS, azure, public IP assignment is
			// dependent on the instance rather than the subnet

			if (associatePublicIp) {
				// This is a master replica, so we assign a public Ip
				// Unlike AWS, we enter a dnsLabel that will resolve to the
				// dynamic public Ip
				withOS = withPublicIpAddress
						.withNewPrimaryPublicIPAddress(clusterNodeId + "-" + ClusterNode.clusterName());
			} else {

				// By default, slave instances are launched without public
				// IPs
				withOS = withPublicIpAddress.withoutPrimaryPublicIPAddress();
			}

			WithCreate withCreate = null;

			// Branch -> osPlatform

			switch (iProfile.getOsPlatform()) {
			case LINUX:

				credentials.withKeyPair(new RSAKeyPair());

				withCreate = withOS.withStoredLinuxImage(iProfile.getImage())
						.withRootUsername(credentials.getUsername()).withRootPassword(credentials.getPassword())
						.withSsh(credentials.getKeyPair().getPublicKey()).withComputerName(resourceName);

				break;
			case WINDOWS:

				withCreate = withOS.withStoredWindowsImage(iProfile.getImage())
						.withAdminUsername(credentials.getUsername()).withAdminPassword(credentials.getPassword())
						.withComputerName(resourceName);

				break;
			}

			ClusterCredentials clusterCredentials = CloudEnvironment.get().credentials();

			VirtualMachine vm = withCreate.withSize(iProfile.getInstanceTypeSpec())
					.withTag(Tags.RESOURCE_NAME_TAG, resourceName).withTag(Tags.NODE_ROLE_TAG, joinWorkflow.toString())

					// Add cluster credentials
					.withTag(ClusterCredentials.CLUSTER_ACCESS_KEY_TAG, clusterCredentials.getAccessKey())
					.withTag(ClusterCredentials.CLUSTER_SECRET_KEY_TAG, clusterCredentials.getSecretKey())

					.create();

			instanceId = vm.vmId();

			// Save Credentials to Registry
			ServiceProviders.getServerRegistry().put(instanceId + "-credentials", credentials.toString());

		} catch (CloudException e) {
			Exceptions.throwRuntime(PlatformException.get(ClusteringError.ERROR_OCCURED_WHILE_MAKING_SERVICE_CALL,
					AzureHelper.name(), e.getMessage()));
		} catch (Exception e) {
			Exceptions.throwRuntime(e);
		}

	}

}
