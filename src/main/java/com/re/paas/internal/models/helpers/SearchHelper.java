package com.re.paas.internal.models.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.google.appengine.api.datastore.QueryResultIterator;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.QueryKeys;
import com.re.paas.internal.base.classes.FluentArrayList;
import com.re.paas.internal.base.classes.IndexedNameType;
import com.re.paas.internal.base.classes.ListingFilter;
import com.re.paas.internal.base.core.CacheType;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.core.fusion.CacheAdapter;
import com.re.paas.internal.entites.directory.PropertyEntity;
import com.re.paas.internal.models.SearchModel;

public class SearchHelper {

	public static List<Long> getKeys(IndexedNameType type, ListingFilter filter, Callable<QueryKeys<PropertyEntity>> fetch) {

		String key = SearchModel.buildCacheListKey(type, FluentArrayList.asList(filter));

		List<Long> result = (List<Long>) CacheAdapter.get(CacheType.PERSISTENT, key);

		if (result == null) {

			CacheHelper.addToListOrCreate(CacheType.PERSISTENT, SearchModel.CACHE_KEY_LIST_ENTRIES, key);

			result = new ArrayList<>();

			QueryResultIterator<Key<PropertyEntity>> it = null;
			try {
				it = fetch.call().iterator();
			} catch (Exception e) {
				Exceptions.throwRuntime(e);
			}

			while (it.hasNext()) {
				result.add(it.next().getId());
			}

			CacheAdapter.put(CacheType.PERSISTENT, key, result);
		}

		return result;
	}
	
	
	public static List<Long> getKeys(IndexedNameType type, String phrase) {

		String key = SearchModel.buildCacheSearchKey(type, phrase);

		List<Long> result = (List<Long>) CacheAdapter.get(CacheType.SHORT_LIVED, key);

		if (result == null) {

			CacheHelper.addToListOrCreate(CacheType.PERSISTENT, SearchModel.CACHE_KEY_SEARCH_ENTRIES, key);

			result = new ArrayList<>();

			for(String s : SearchModel.search(type, phrase)){
				result.add(Long.parseLong(s));
			};

			CacheAdapter.put(CacheType.SHORT_LIVED, key, result);
		}

		return result;
	}
	
}
