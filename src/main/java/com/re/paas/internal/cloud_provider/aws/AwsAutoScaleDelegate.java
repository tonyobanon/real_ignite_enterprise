package com.re.paas.internal.cloud_provider.aws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsRequest;
import com.amazonaws.services.ec2.model.DescribeSubnetsResult;
import com.amazonaws.services.ec2.model.DescribeVpcAttributeRequest;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.util.EC2MetadataUtils;
import com.re.paas.internal.base.core.DefaultLogger;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.cloud_provider.AutoScaleDelegate;
import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.cloud_provider.Tags;
import com.re.paas.internal.clustering.ClusterNode;
import com.re.paas.internal.clustering.classes.ClusterCredentials;
import com.re.paas.internal.clustering.classes.ClusterJoinWorkflow;
import com.re.paas.internal.clustering.classes.InstanceProfile;
import com.re.paas.internal.clustering.classes.JoinWorkflow;
import com.re.paas.internal.clustering.classes.MasterNodeConfig;
import com.re.paas.internal.clustering.classes.ServiceProviders;
import com.re.paas.internal.clustering.classes.StaticInstanceTypes;
import com.re.paas.internal.clustering.classes.Utils;
import com.re.paas.internal.clustering.nic_utils.IPAddresses;
import com.re.paas.internal.clustering.nic_utils.NIC;
import com.re.paas.internal.errors.ClusteringError;

public class AwsAutoScaleDelegate extends AutoScaleDelegate {

	@Override
	public String getInstanceId() {
		return AWSHelper.getInstanceMetaData("instance-id");
	}

	@Override
	public InstanceProfile getInstanceProfile() {

		InstanceProfile profile = null;

		try {

			AmazonEC2 ec2 = AWSHelper.getEC2Client();

			// ** Validate Instance Type Spec
			String instanceType = MasterNodeConfig.get(MasterNodeConfig.AWS_INSTANCE_TYPE);
			if ((!AWSHelper.isValidInstanceType(instanceType))
					&& (!instanceType.equals(StaticInstanceTypes.INHERIT))
					&& (!instanceType.equals(StaticInstanceTypes.DYNAMIC))) {

				Exceptions.throwRuntime(
						PlatformException.get(ClusteringError.INSTANCE_TYPE_HAS_INCORRECT_FORMAT, instanceType));
			}

			// ** Validate Region
			String region = MasterNodeConfig.getOrNull(MasterNodeConfig.REGION);
			if (region != null) {
				if (!AWSHelper.isValidRegion(region)) {
					Exceptions.throwRuntime(
							PlatformException.get(ClusteringError.REGION_HAS_INCORRECT_FORMAT, region));
				}
			} else {
				region = EC2MetadataUtils.getEC2InstanceRegion();
			}

			String publicIp = AWSHelper.getInstanceMetaData("public-ipv4");

			// Validate public ip
			if (publicIp == null) {
				
				Exceptions.throwRuntime(
						PlatformException.get(ClusteringError.MASTER_DOES_NOT_HAVE_ROUTEABLE_ADDRESS, ClusterNode.getWKA().getHostAddress()));
				
			}

			String publicDnsName = AWSHelper.getInstanceMetaData("public-hostname");

			// Validate public dns name
			if (publicDnsName == null) {
				Exceptions
						.throwRuntime(PlatformException.get(ClusteringError.MASTER_DOES_NOT_HAVE_PUBLIC_DNS_NAME));
			}

			String mac = Utils
					.toMACAddress(NIC.getMatchingNIC(ClusterNode.getClusteringAddress()).getHardwareAddress());
			String vpcId = AWSHelper.getInstanceMetaData("network/interfaces/macs/" + mac + "/vpc-id");

			// Validate that the primary network interface is connected a
			// virtual network
			if (vpcId == null) {

				Exceptions.throwRuntime(PlatformException.get(ClusteringError.MASTER_NOT_IN_VIRTUAL_PRIVATE_NETWORK,
						ClusterNode.getWKA().getHostAddress()));

			}

			// For this Vpc, the enableDnsHostnames attribute must be true
			Boolean isDnsNameEnabled = ec2
					.describeVpcAttribute(
							new DescribeVpcAttributeRequest().withVpcId(vpcId).withAttribute("enableDnsHostnames"))
					.isEnableDnsHostnames();
			if (!isDnsNameEnabled) {
				Exceptions.throwRuntime(
						PlatformException.get(ClusteringError.DNS_HOSTNAMES_ARE_CURRENTLY_DISABLED, vpcId));

			}

			String masterSubnetId = AWSHelper.getInstanceMetaData("network/interfaces/macs/" + mac + "/subnet-id");

			com.amazonaws.services.ec2.model.Subnet masterSubnet = ec2.describeSubnets(
					new DescribeSubnetsRequest().withSubnetIds(masterSubnetId).withFilters(new Filter("cidrBlock")))
					.getSubnets().get(0);

			// Instances launched in it should automatically have public IPs
			if (!masterSubnet.isMapPublicIpOnLaunch()) {
				Exceptions.throwRuntime(PlatformException
						.get(ClusteringError.NOT_MAPPING_PUBLIC_IPS_INSTANCES_IN_SUBNET, masterSubnetId));
			}

			String masterSubnetCIDR = masterSubnet.getCidrBlock();

			// ** Validate private subnet id
			String slaveSubnetId = MasterNodeConfig.get(MasterNodeConfig.PRIVATE_SUBNET_ID);

			DescribeSubnetsResult slaveSubnetResult = ec2
					.describeSubnets(new DescribeSubnetsRequest().withSubnetIds(slaveSubnetId)
							.withFilters(new Filter("subnet-id"), new Filter("vpc-id"), new Filter("cidrBlock")));

			com.amazonaws.services.ec2.model.Subnet slaveSubnet = null;

			if (slaveSubnetResult.getSubnets().isEmpty()) {
				// slaveSubnetId is invalid

				Exceptions.throwRuntime(
						PlatformException.get(ClusteringError.SUBNET_NOT_FOUND_ON_NETWORK, slaveSubnetId, vpcId));
			}

			slaveSubnet = slaveSubnetResult.getSubnets().get(0);

			if (!slaveSubnet.getVpcId().equals(vpcId)) {
				// slaveSubnetId belongs to a different vpc
				Exceptions.throwRuntime(
						PlatformException.get(ClusteringError.SUBNET_NOT_FOUND_ON_NETWORK, slaveSubnetId, vpcId));
			}

			if (slaveSubnet.isMapPublicIpOnLaunch()) {
				Logger.get().warn("The <slave> subnet:" + slaveSubnetId
						+ " assigns public IPs to instances launches in it. This is not recommended..");
			}

			String slaveSubnetCIDR = slaveSubnet.getCidrBlock();

			// Build Instance Profile

			profile = new InstanceProfile();

			profile.withImage(EC2MetadataUtils.getAmiId())
					.withDefaultInstanceTypeSpec(EC2MetadataUtils.getInstanceType())
					.withInstanceTypeSpec(instanceType).withDefaultRegion(EC2MetadataUtils.getEC2InstanceRegion())
					.withRegion(region).withVirtualNetworkId(vpcId).withMasterSubnetId(masterSubnetId)
					.withMasterSubnetCIDR(masterSubnetCIDR).withSlaveSubnetId(slaveSubnetId)
					.withSlaveSubnetCIDR(slaveSubnetCIDR).withMasterPublicIp(publicIp)
					.withOsPlatform(Utils.getPlatform()).withSecurityGroup(EC2MetadataUtils.getSecurityGroups())
					.withMasterPublicDNSName(publicDnsName);

		} catch (AmazonServiceException e) {

			Exceptions.throwRuntime(PlatformException.get(ClusteringError.ERROR_OCCURED_WHILE_MAKING_SERVICE_CALL,
					AWSHelper.name(), e.getMessage()));

		} catch (AmazonClientException | IOException e) {
			Exceptions.throwRuntime(e);
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


		try {

			AmazonEC2 ec2 = AWSHelper.getEC2Client(iProfile.getRegion());

			// Generate credentials (Create Key Pair)
			KeyPair keyPair = ec2.createKeyPair(new CreateKeyPairRequest().withKeyName(Utils.newSecureRandom()))
					.getKeyPair();

			Ec2InstanceCredential credentials = new Ec2InstanceCredential(keyPair.getKeyName(),
					keyPair.getKeyFingerprint(), keyPair.getKeyMaterial());

			String resourceName = ClusterNode.getResourceName(joinWorkflow);

			RunInstancesRequest amazonVM = new RunInstancesRequest();

			// Unlike azure, an attribute exists per subnet to determine
			// whether instances launched in it have public or private
			// addresses

			// Also, unlike azure we do not need to explicitly specify the
			// virtual network id, the subnet already has that info

			// In AWS, security-groups are applied instance-level, and so it
			// is applied to all new cluster instances created

			// TODO: Probe Instance Initiated Shutdown Behavior

			amazonVM.withImageId(iProfile.getImage()).withInstanceType(iProfile.getInstanceTypeSpec())
					.withDisableApiTermination(true).withMinCount(1).withMaxCount(1)
					.withKeyName(keyPair.getKeyName()).withSecurityGroups(iProfile.getSecurityGroup())
					.withPrivateIpAddress(privateIp).withSubnetId(subnetId);

			Map<String, String> userData = new HashMap<String, String>(3);
			userData.put(Tags.RESOURCE_NAME_TAG, resourceName);
			userData.put(Tags.NODE_ROLE_TAG, joinWorkflow.toString());
			
			// Add cluster credentials
			
			ClusterCredentials clusterCredentials = CloudEnvironment.get().credentials();
			
			userData.put(ClusterCredentials.CLUSTER_ACCESS_KEY_TAG, clusterCredentials.getAccessKey());
			userData.put(ClusterCredentials.CLUSTER_SECRET_KEY_TAG, clusterCredentials.getSecretKey());
			
			amazonVM.withUserData(AWSHelper.toUserData(userData));

			RunInstancesResult result = ec2.runInstances(amazonVM);

			Instance vm = result.getReservation().getInstances().get(0);
			instanceId = vm.getInstanceId();

			// Include a tag for this the resource name, in this instance
			ec2.createTags(new CreateTagsRequest().withResources(instanceId).withTags(
					new Tag(Tags.RESOURCE_NAME_TAG, resourceName), new Tag(Tags.NODE_ROLE_TAG, joinWorkflow.toString())));

			// Save Credentials to Registry
			ServiceProviders.getServerRegistry().put(instanceId + "-credentials", credentials.toString());

		} catch (AmazonServiceException e) {
			Exceptions.throwRuntime(PlatformException.get(ClusteringError.ERROR_OCCURED_WHILE_MAKING_SERVICE_CALL,
					AWSHelper.name(), e.getMessage()));
		} catch (AmazonClientException e) {
			Exceptions.throwRuntime(e);
		}
		
		
		
	}

}
