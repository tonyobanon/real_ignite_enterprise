package com.re.paas.internal.models;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.VoidWork;
import com.re.paas.internal.base.classes.InstallOptions;
import com.re.paas.internal.base.core.PlatformInternal;
import com.re.paas.internal.base.core.ThreadContext;
import com.re.paas.internal.core.keys.ConfigKeys;
import com.re.paas.internal.utils.LocaleUtils;

@Model(dependencies = ConfigModel.class)
public class LocaleModel implements BaseModel {

	private static String defaultLocale;

	public static final String USER_DEFAULT_LOCALE = "USER_DEFAULT_LOCALE";
	public static final String USER_DEFAULT_NUMBER_FORMAT = "USER_DEFAULT_NUMBER_FORMAT";

	@Override
	public String path() {
		return "core/locale";
	}

	@Override
	public void install(InstallOptions options) {

		Locale locale = Locale
				.forLanguageTag(LocaleUtils.buildLocaleString(options.getLanguage(), options.getCountry()));
		ConfigModel.put(ConfigKeys.DEFAULT_LOCALE, locale.toLanguageTag());
	}

	@Override
	public void start() {
		ObjectifyService.run(new VoidWork() {
			public void vrun() {
				defaultLocale = _defaultLocale();
			}
		});
	}

	protected static String _defaultLocale() {
		return ConfigModel.get(ConfigKeys.DEFAULT_LOCALE);
	}

	public static String defaultLocale() {
		String defaultLocale = LocaleModel.defaultLocale != null ? LocaleModel.defaultLocale : _defaultLocale();
		return defaultLocale != null ? defaultLocale : 
			//The platform may not have been installed
			"en-US";
	}

	protected static String defaultTimezone() {
		return ConfigModel.get(ConfigKeys.DEFAULT_TIMEZONE);
	}

	protected static void setDefaultLocale(Locale locale) {
		ConfigModel.put(ConfigKeys.DEFAULT_LOCALE, locale.toString());
	}

	@PlatformInternal
	public static void setUserLocale(List<String> acceptableLocales, Long principal) {

		String defaultLocal = !acceptableLocales.isEmpty() ? acceptableLocales.get(0) : null;

		if (defaultLocal == null && principal != null) {
			defaultLocal = BaseUserModel.getPreferredLocale(principal);
		} else {
			defaultLocal = LocaleModel.defaultLocale();
		}

		Locale locale = Locale.forLanguageTag(defaultLocal);

		ThreadContext.set(USER_DEFAULT_LOCALE, locale.toLanguageTag());
		ThreadContext.set(USER_DEFAULT_NUMBER_FORMAT, NumberFormat.getCurrencyInstance(locale));
	}

	protected static String getUserLocale() {
		return ThreadContext.isRequestContext() ? ThreadContext.get(USER_DEFAULT_LOCALE).toString() : defaultLocale();
	}

	protected static NumberFormat getUserNumberFormat() {
		return (NumberFormat) ThreadContext.get(USER_DEFAULT_NUMBER_FORMAT);
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
