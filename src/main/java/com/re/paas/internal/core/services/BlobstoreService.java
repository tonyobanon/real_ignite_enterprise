package com.re.paas.internal.core.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.re.paas.internal.base.classes.spec.BlobSpec;
import com.re.paas.internal.base.core.GsonFactory;
import com.re.paas.internal.core.fusion.BaseService;
import com.re.paas.internal.core.fusion.FusionEndpoint;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.models.BlobStoreModel;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class BlobstoreService extends BaseService { 
@Override
public String uri() {
	return "/blobstore";
}
	@FusionEndpoint(uri = "/save", method = HttpMethod.PUT, isBlocking = true, enableMultipart = true,
			functionality = Functionality.SAVE_BINARY_DATA)
	public void saveBlob(RoutingContext ctx) {
		List<String> blobIds = new ArrayList<>();
		ctx.fileUploads().forEach(f -> {
			try {
				String blobId = BlobStoreModel.save(null, Files.newInputStream(Paths.get(f.uploadedFileName())));
				blobIds.add(blobId);
			} catch (IOException e) {
				// Silently fail
			}
		});
		ctx.response()
		.write(GsonFactory.getInstance().toJson(blobIds)).setChunked(true)
		.end();
	}

	@FusionEndpoint(uri = "/get", requestParams = { "blobId" }, createXhrClient = false,
			functionality = Functionality.GET_BINARY_DATA, cache = true)
	public void getBlob(RoutingContext ctx) {
		String blobId = ctx.request().getParam("blobId");
		BlobSpec blob = BlobStoreModel.get(blobId);
		ctx.response()
		.putHeader("Content-Type", blob.getMimeType())
		.putHeader("Content-Length", blob.getSize().toString())
		.bodyEndHandler(v -> {
			ctx.response().end();
		}).write(Buffer.buffer(blob.getData()));
	} 
 
	@FusionEndpoint(uri = "/delete", requestParams = { "blobId" },
			functionality = Functionality.MANAGE_BINARY_DATA)
	public void deleteBlob(RoutingContext ctx) {
		String blobId = ctx.request().getParam("blobId");
		BlobStoreModel.delete(blobId);
	}
	
	@FusionEndpoint(uri = "/list",
			functionality = Functionality.MANAGE_BINARY_DATA)
	public void list(RoutingContext ctx) {
		List<BlobSpec> entries = BlobStoreModel.list();
		ctx.response()
		.write(GsonFactory.getInstance().toJson(entries)).setChunked(true)
		.end();
	}
}
