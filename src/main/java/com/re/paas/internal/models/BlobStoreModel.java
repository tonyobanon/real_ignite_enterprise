package com.re.paas.internal.models;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SerializationUtils;

import com.re.paas.internal.base.classes.FluentArrayList;
import com.re.paas.internal.base.classes.InstallOptions;
import com.re.paas.internal.base.classes.spec.BlobSpec;
import com.re.paas.internal.base.core.BlockerTodo;
import com.re.paas.internal.base.core.Todo;
import com.re.paas.internal.core.fusion.WebRoutes;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.entites.BlobEntity;
import com.re.paas.internal.models.helpers.Dates;
import com.re.paas.internal.models.helpers.EntityHelper;
import com.re.paas.internal.utils.Utils;

@BlockerTodo("Deprecate this functionality in favour of the GAE Blob Service")
@Todo("Add functionality that enables the administrator to create arbitary rules for blob storage, use: MANAGE_BINARY_DATA")
public class BlobStoreModel implements BaseModel {

	@Override
	public String path() {
		return "core/blobstore";
	}
	
	@Override
	public void install(InstallOptions options) {
	}

	@Override
	public void start() {

	}

	protected static String save(String id, Serializable obj) throws IOException {
		return save(id, SerializationUtils.serialize(obj));
	}

	public static String save(InputStream in) throws IOException {
		return save(null, in);
	}

	@ModelMethod(functionality = Functionality.SAVE_BINARY_DATA)
	public static String save(@Nullable String id, InputStream in) throws IOException {
		return save(id, IOUtils.toByteArray(in));
	}

	@ModelMethod(functionality = Functionality.SAVE_BINARY_DATA)
	public static String save(String id, byte[] data) throws IOException {

		if (id == null) {
			id = Utils.newRandom();
		}

		String mimeType = WebRoutes.TIKA_INSTANCE.detect(data);

		int size = data.length;

		BlobEntity entity = new BlobEntity().setId(id).setData(data).setSize(size).setMimeType(mimeType)
				.setDateCreated(Dates.now());

		ofy().save().entity(entity).now();

		return entity.getId();
	}

	@ModelMethod(functionality = Functionality.MANAGE_BINARY_DATA)
	public static List<BlobSpec> list() {
		List<BlobSpec> result = new FluentArrayList<>();
		ofy().load().type(BlobEntity.class).forEach(e -> {
			result.add(EntityHelper.toObjectModel(e));
		});
		return result;
	}

	@ModelMethod(functionality = Functionality.MANAGE_BINARY_DATA)
	public static void delete(String id) {
		ofy().delete().type(BlobEntity.class).id(id).now();
	}

	@ModelMethod(functionality = Functionality.GET_BINARY_DATA)
	public static BlobSpec get(String id) {
		BlobEntity entity = ofy().load().type(BlobEntity.class).id(id).safe();
		return EntityHelper.toObjectModel(entity);
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preInstall() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unInstall() {
		// TODO Auto-generated method stub
		
	}
}
