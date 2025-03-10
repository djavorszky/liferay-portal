/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.journal.service.impl;

import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.model.AssetLink;
import com.liferay.asset.kernel.model.AssetLinkConstants;
import com.liferay.asset.kernel.model.AssetRenderer;
import com.liferay.asset.kernel.model.AssetRendererFactory;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.util.DLUtil;
import com.liferay.document.library.util.DLURLHelper;
import com.liferay.dynamic.data.mapping.exception.StructureDefinitionException;
import com.liferay.dynamic.data.mapping.model.DDMForm;
import com.liferay.dynamic.data.mapping.model.DDMFormField;
import com.liferay.dynamic.data.mapping.model.DDMStorageLink;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMStructureLink;
import com.liferay.dynamic.data.mapping.model.DDMTemplate;
import com.liferay.dynamic.data.mapping.model.LocalizedValue;
import com.liferay.dynamic.data.mapping.service.DDMStorageLinkLocalService;
import com.liferay.dynamic.data.mapping.service.DDMStructureLinkLocalService;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLinkLocalService;
import com.liferay.dynamic.data.mapping.service.DDMTemplateLocalService;
import com.liferay.expando.kernel.util.ExpandoBridgeUtil;
import com.liferay.exportimport.kernel.exception.ExportImportContentValidationException;
import com.liferay.exportimport.kernel.lar.ExportImportThreadLocal;
import com.liferay.friendly.url.model.FriendlyURLEntry;
import com.liferay.friendly.url.service.FriendlyURLEntryLocalService;
import com.liferay.journal.configuration.JournalGroupServiceConfiguration;
import com.liferay.journal.configuration.JournalServiceConfiguration;
import com.liferay.journal.constants.JournalActivityKeys;
import com.liferay.journal.constants.JournalConstants;
import com.liferay.journal.exception.ArticleExpirationDateException;
import com.liferay.journal.exception.ArticleFriendlyURLException;
import com.liferay.journal.exception.ArticleReviewDateException;
import com.liferay.journal.exception.ArticleVersionException;
import com.liferay.journal.exception.DuplicateArticleIdException;
import com.liferay.journal.exception.NoSuchArticleException;
import com.liferay.journal.exception.RequiredArticleLocalizationException;
import com.liferay.journal.internal.util.JournalTreePathUtil;
import com.liferay.journal.internal.util.JournalUtil;
import com.liferay.journal.internal.validation.JournalArticleModelValidator;
import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.model.JournalArticleConstants;
import com.liferay.journal.model.JournalArticleDisplay;
import com.liferay.journal.model.JournalArticleLocalization;
import com.liferay.journal.model.JournalArticleResource;
import com.liferay.journal.model.JournalFolder;
import com.liferay.journal.model.JournalFolderConstants;
import com.liferay.journal.model.impl.JournalArticleDisplayImpl;
import com.liferay.journal.service.JournalArticleResourceLocalService;
import com.liferay.journal.service.JournalContentSearchLocalService;
import com.liferay.journal.service.base.JournalArticleLocalServiceBaseImpl;
import com.liferay.journal.util.JournalDefaultTemplateProvider;
import com.liferay.journal.util.JournalHelper;
import com.liferay.journal.util.comparator.ArticleIDComparator;
import com.liferay.journal.util.comparator.ArticleVersionComparator;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.petra.xml.XMLUtil;
import com.liferay.portal.aop.AopService;
import com.liferay.portal.kernel.comment.CommentManager;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.DefaultActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.IndexableActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryDefinition;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.diff.DiffHtmlUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.json.JSONUtil;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Image;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.SystemEventConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.WorkflowDefinitionLink;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.notifications.UserNotificationDefinition;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.PortletProvider;
import com.liferay.portal.kernel.portlet.PortletProviderUtil;
import com.liferay.portal.kernel.portlet.PortletRequestModel;
import com.liferay.portal.kernel.portletfilerepository.PortletFileRepositoryUtil;
import com.liferay.portal.kernel.repository.capabilities.TemporaryFileEntriesCapability;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.repository.model.Folder;
import com.liferay.portal.kernel.sanitizer.SanitizerUtil;
import com.liferay.portal.kernel.search.BaseModelSearchResult;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.Hits;
import com.liferay.portal.kernel.search.Indexable;
import com.liferay.portal.kernel.search.IndexableType;
import com.liferay.portal.kernel.search.Indexer;
import com.liferay.portal.kernel.search.IndexerRegistryUtil;
import com.liferay.portal.kernel.search.QueryConfig;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.Sort;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.permission.ModelPermissions;
import com.liferay.portal.kernel.settings.GroupServiceSettingsLocator;
import com.liferay.portal.kernel.social.SocialActivityManagerUtil;
import com.liferay.portal.kernel.systemevent.SystemEvent;
import com.liferay.portal.kernel.systemevent.SystemEventHierarchyEntryThreadLocal;
import com.liferay.portal.kernel.template.TemplateConstants;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.CalendarFactoryUtil;
import com.liferay.portal.kernel.util.Constants;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.FriendlyURLNormalizerUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.GroupSubscriptionCheckSubscriptionSender;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.Http;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.MathUtil;
import com.liferay.portal.kernel.util.ObjectValuePair;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Portal;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.SubscriptionSender;
import com.liferay.portal.kernel.util.TempFileEntryUtil;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowHandler;
import com.liferay.portal.kernel.workflow.WorkflowHandlerRegistryUtil;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.Node;
import com.liferay.portal.kernel.xml.SAXReaderUtil;
import com.liferay.portal.kernel.xml.XPath;
import com.liferay.portal.validation.ModelValidator;
import com.liferay.portal.validation.ModelValidatorRegistryUtil;
import com.liferay.social.kernel.model.SocialActivityConstants;
import com.liferay.subscription.service.SubscriptionLocalService;
import com.liferay.trash.TrashHelper;
import com.liferay.trash.exception.RestoreEntryException;
import com.liferay.trash.exception.TrashEntryException;
import com.liferay.trash.kernel.model.TrashEntry;
import com.liferay.trash.kernel.model.TrashVersion;
import com.liferay.upload.AttachmentContentUpdater;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Provides the local service for accessing, adding, deleting, and updating web
 * content articles.
 *
 * @author Brian Wing Shun Chan
 * @author Raymond Augé
 * @author Bruno Farache
 * @author Juan Fernández
 * @author Sergio González
 */
@Component(
	property = "model.class.name=com.liferay.journal.model.JournalArticle",
	service = AopService.class
)
public class JournalArticleLocalServiceImpl
	extends JournalArticleLocalServiceBaseImpl {

	/**
	 * Adds a web content article with additional parameters. All scheduling
	 * parameters (display date, expiration date, and review date) use the
	 * current user's timezone.
	 *
	 * <p>
	 * The web content articles hold HTML content wrapped in XML. The XML lets
	 * you specify the article's default locale and available locales. Here is a
	 * content example:
	 * </p>
	 *
	 * <p>
	 * <pre>
	 * <code>
	 * &lt;?xml version='1.0' encoding='UTF-8'?&gt;
	 * &lt;root default-locale="en_US" available-locales="en_US"&gt;
	 * 	&lt;static-content language-id="en_US"&gt;
	 * 		&lt;![CDATA[&lt;p&gt;&lt;b&gt;&lt;i&gt;test&lt;i&gt; content&lt;b&gt;&lt;/p&gt;]]&gt;
	 * 	&lt;/static-content&gt;
	 * &lt;/root&gt;
	 * </code>
	 * </pre></p>
	 *
	 * @param  userId the primary key of the web content article's creator/owner
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article folder
	 * @param  classNameId the primary key of the DDMStructure class if the web
	 *         content article is related to a DDM structure, the primary key of
	 *         the class name associated with the article, or
	 *         JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *         module otherwise
	 * @param  classPK the primary key of the DDM structure, if the primary key
	 *         of the DDMStructure class is given as the
	 *         <code>classNameId</code> parameter, the primary key of the class
	 *         associated with the web content article, or <code>0</code>
	 *         otherwise
	 * @param  articleId the primary key of the web content article
	 * @param  autoArticleId whether to auto generate the web content article ID
	 * @param  version the web content article's version
	 * @param  titleMap the web content article's locales and localized titles
	 * @param  descriptionMap the web content article's locales and localized
	 *         descriptions
	 * @param  friendlyURLMap the web content article's locales and localized
	 *         friendly URLs
	 * @param  content the HTML content wrapped in XML
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure, if the article is related to a DDM structure, or
	 *         <code>null</code> otherwise
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  layoutUuid the unique string identifying the web content
	 *         article's display page
	 * @param  displayDateMonth the month the web content article is set to
	 *         display
	 * @param  displayDateDay the calendar day the web content article is set to
	 *         display
	 * @param  displayDateYear the year the web content article is set to
	 *         display
	 * @param  displayDateHour the hour the web content article is set to
	 *         display
	 * @param  displayDateMinute the minute the web content article is set to
	 *         display
	 * @param  expirationDateMonth the month the web content article is set to
	 *         expire
	 * @param  expirationDateDay the calendar day the web content article is set
	 *         to expire
	 * @param  expirationDateYear the year the web content article is set to
	 *         expire
	 * @param  expirationDateHour the hour the web content article is set to
	 *         expire
	 * @param  expirationDateMinute the minute the web content article is set to
	 *         expire
	 * @param  neverExpire whether the web content article is not set to auto
	 *         expire
	 * @param  reviewDateMonth the month the web content article is set for
	 *         review
	 * @param  reviewDateDay the calendar day the web content article is set for
	 *         review
	 * @param  reviewDateYear the year the web content article is set for review
	 * @param  reviewDateHour the hour the web content article is set for review
	 * @param  reviewDateMinute the minute the web content article is set for
	 *         review
	 * @param  neverReview whether the web content article is not set for review
	 * @param  indexable whether the web content article is searchable
	 * @param  smallImage whether the web content article has a small image
	 * @param  smallImageURL the web content article's small image URL
	 * @param  smallImageFile the web content article's small image file
	 * @param  images the web content's images
	 * @param  articleURL the web content article's accessible URL
	 * @param  serviceContext the service context to be applied. Can set the
	 *         UUID, creation date, modification date, expando bridge
	 *         attributes, guest permissions, group permissions, asset category
	 *         IDs, asset tag names, asset link entry IDs, URL title, and
	 *         workflow actions for the web content article. Can also set
	 *         whether to add the default guest and group permissions.
	 * @return the web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle addArticle(
			long userId, long groupId, long folderId, long classNameId,
			long classPK, String articleId, boolean autoArticleId,
			double version, Map<Locale, String> titleMap,
			Map<Locale, String> descriptionMap,
			Map<Locale, String> friendlyURLMap, String content,
			String ddmStructureKey, String ddmTemplateKey, String layoutUuid,
			int displayDateMonth, int displayDateDay, int displayDateYear,
			int displayDateHour, int displayDateMinute, int expirationDateMonth,
			int expirationDateDay, int expirationDateYear,
			int expirationDateHour, int expirationDateMinute,
			boolean neverExpire, int reviewDateMonth, int reviewDateDay,
			int reviewDateYear, int reviewDateHour, int reviewDateMinute,
			boolean neverReview, boolean indexable, boolean smallImage,
			String smallImageURL, File smallImageFile,
			Map<String, byte[]> images, String articleURL,
			ServiceContext serviceContext)
		throws PortalException {

		// Article

		User user = userLocalService.getUser(userId);
		articleId = StringUtil.toUpperCase(StringUtil.trim(articleId));

		Date displayDate = _portal.getDate(
			displayDateMonth, displayDateDay, displayDateYear, displayDateHour,
			displayDateMinute, user.getTimeZone(), null);

		Date expirationDate = null;
		Date reviewDate = null;

		if (!neverExpire) {
			expirationDate = _portal.getDate(
				expirationDateMonth, expirationDateDay, expirationDateYear,
				expirationDateHour, expirationDateMinute, user.getTimeZone(),
				ArticleExpirationDateException.class);
		}

		if (!neverReview) {
			reviewDate = _portal.getDate(
				reviewDateMonth, reviewDateDay, reviewDateYear, reviewDateHour,
				reviewDateMinute, user.getTimeZone(),
				ArticleReviewDateException.class);
		}

		byte[] smallImageBytes = null;

		try {
			smallImageBytes = FileUtil.getBytes(smallImageFile);
		}
		catch (IOException ioe) {
		}

		boolean validate = !ExportImportThreadLocal.isImportInProcess();

		if (validate) {
			validateDDMStructureId(groupId, folderId, ddmStructureKey);
		}

		if (autoArticleId) {
			articleId = String.valueOf(counterLocalService.increment());
		}

		sanitize(user.getCompanyId(), groupId, userId, classPK, descriptionMap);

		if (validate) {
			validate(
				user.getCompanyId(), groupId, classNameId, articleId,
				autoArticleId, version, titleMap, content, ddmStructureKey,
				ddmTemplateKey, displayDate, expirationDate, smallImage,
				smallImageURL, smallImageFile, smallImageBytes, serviceContext);

			try {
				validateReferences(
					groupId, ddmStructureKey, ddmTemplateKey, layoutUuid,
					smallImage, smallImageURL, smallImageBytes, 0, content);
			}
			catch (ExportImportContentValidationException eicve) {
				eicve.setStagedModelClassName(JournalArticle.class.getName());
				eicve.setStagedModelPrimaryKeyObj(articleId);

				throw eicve;
			}
		}

		serviceContext.setAttribute("articleId", articleId);

		long id = counterLocalService.increment();

		String articleResourceUuid = GetterUtil.getString(
			serviceContext.getAttribute("articleResourceUuid"));

		long resourcePrimKey =
			_journalArticleResourceLocalService.getArticleResourcePrimKey(
				articleResourceUuid, groupId, articleId);

		JournalArticle article = journalArticlePersistence.create(id);

		Locale locale = getArticleDefaultLocale(content);

		friendlyURLMap = _checkFriendlyURLMap(locale, friendlyURLMap, titleMap);

		Map<String, String> urlTitleMap = _getURLTitleMap(
			groupId, resourcePrimKey, friendlyURLMap, titleMap);

		String urlTitle = urlTitleMap.get(LocaleUtil.toLanguageId(locale));

		article.setUuid(serviceContext.getUuid());
		article.setResourcePrimKey(resourcePrimKey);
		article.setGroupId(groupId);
		article.setCompanyId(user.getCompanyId());
		article.setUserId(user.getUserId());
		article.setUserName(user.getFullName());
		article.setFolderId(folderId);
		article.setClassNameId(classNameId);
		article.setClassPK(classPK);
		article.setTreePath(article.buildTreePath());
		article.setArticleId(articleId);
		article.setVersion(version);
		article.setUrlTitle(urlTitle);

		content = format(user, groupId, article, content);
		content = _replaceTempImages(article, content);

		article.setContent(content);

		article.setDDMStructureKey(ddmStructureKey);
		article.setDDMTemplateKey(ddmTemplateKey);
		article.setDefaultLanguageId(LocaleUtil.toLanguageId(locale));
		article.setLayoutUuid(layoutUuid);
		article.setDisplayDate(displayDate);
		article.setExpirationDate(expirationDate);
		article.setReviewDate(reviewDate);
		article.setIndexable(indexable);
		article.setSmallImage(smallImage);
		article.setSmallImageId(counterLocalService.increment());
		article.setSmallImageURL(smallImageURL);

		Date now = new Date();

		if ((expirationDate == null) || expirationDate.after(now)) {
			article.setStatus(WorkflowConstants.STATUS_DRAFT);
		}
		else {
			article.setStatus(WorkflowConstants.STATUS_EXPIRED);
		}

		article.setStatusByUserId(userId);
		article.setStatusDate(serviceContext.getModifiedDate(now));
		article.setExpandoBridgeAttributes(serviceContext);

		article = journalArticlePersistence.update(article);

		// Friendly URLs

		updateFriendlyURLs(article, urlTitleMap, serviceContext);

		// Article localization

		_addArticleLocalizedFields(
			user.getCompanyId(), article.getId(), titleMap, descriptionMap);

		// Resources

		if (serviceContext.isAddGroupPermissions() ||
			serviceContext.isAddGuestPermissions()) {

			addArticleResources(
				article, serviceContext.isAddGroupPermissions(),
				serviceContext.isAddGuestPermissions());
		}
		else {
			addArticleResources(article, serviceContext.getModelPermissions());
		}

		// Small image

		saveImages(
			smallImage, article.getSmallImageId(), smallImageFile,
			smallImageBytes);

		// Asset

		updateAsset(
			userId, article, serviceContext.getAssetCategoryIds(),
			serviceContext.getAssetTagNames(),
			serviceContext.getAssetLinkEntryIds(),
			serviceContext.getAssetPriority());

		// Dynamic data mapping

		if (classNameLocalService.getClassNameId(DDMStructure.class) ==
				classNameId) {

			updateDDMStructurePredefinedValues(
				classPK, content, serviceContext);
		}
		else {
			updateDDMLinks(id, groupId, ddmStructureKey, ddmTemplateKey, true);
		}

		// Email

		articleURL = buildArticleURL(articleURL, groupId, folderId, articleId);

		serviceContext.setAttribute("articleURL", articleURL);

		sendEmail(article, articleURL, "requested", serviceContext);

		// Workflow

		startWorkflowInstance(userId, article, serviceContext);

		return article;
	}

	/**
	 * Adds a web content article with additional parameters. All scheduling
	 * parameters (display date, expiration date, and review date) use the
	 * current user's timezone.
	 *
	 * <p>
	 * The web content articles hold HTML content wrapped in XML. The XML lets
	 * you specify the article's default locale and available locales. Here is a
	 * content example:
	 * </p>
	 *
	 * <p>
	 * <pre>
	 * <code>
	 * &lt;?xml version='1.0' encoding='UTF-8'?&gt;
	 * &lt;root default-locale="en_US" available-locales="en_US"&gt;
	 * 	&lt;static-content language-id="en_US"&gt;
	 * 		&lt;![CDATA[&lt;p&gt;&lt;b&gt;&lt;i&gt;test&lt;i&gt; content&lt;b&gt;&lt;/p&gt;]]&gt;
	 * 	&lt;/static-content&gt;
	 * &lt;/root&gt;
	 * </code>
	 * </pre></p>
	 *
	 * @param  userId the primary key of the web content article's creator/owner
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article folder
	 * @param  classNameId the primary key of the DDMStructure class if the web
	 *         content article is related to a DDM structure, the primary key of
	 *         the class name associated with the article, or
	 *         JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *         module otherwise
	 * @param  classPK the primary key of the DDM structure, if the primary key
	 *         of the DDMStructure class is given as the
	 *         <code>classNameId</code> parameter, the primary key of the class
	 *         associated with the web content article, or <code>0</code>
	 *         otherwise
	 * @param  articleId the primary key of the web content article
	 * @param  autoArticleId whether to auto generate the web content article ID
	 * @param  version the web content article's version
	 * @param  titleMap the web content article's locales and localized titles
	 * @param  descriptionMap the web content article's locales and localized
	 *         descriptions
	 * @param  content the HTML content wrapped in XML
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure, if the article is related to a DDM structure, or
	 *         <code>null</code> otherwise
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  layoutUuid the unique string identifying the web content
	 *         article's display page
	 * @param  displayDateMonth the month the web content article is set to
	 *         display
	 * @param  displayDateDay the calendar day the web content article is set to
	 *         display
	 * @param  displayDateYear the year the web content article is set to
	 *         display
	 * @param  displayDateHour the hour the web content article is set to
	 *         display
	 * @param  displayDateMinute the minute the web content article is set to
	 *         display
	 * @param  expirationDateMonth the month the web content article is set to
	 *         expire
	 * @param  expirationDateDay the calendar day the web content article is set
	 *         to expire
	 * @param  expirationDateYear the year the web content article is set to
	 *         expire
	 * @param  expirationDateHour the hour the web content article is set to
	 *         expire
	 * @param  expirationDateMinute the minute the web content article is set to
	 *         expire
	 * @param  neverExpire whether the web content article is not set to auto
	 *         expire
	 * @param  reviewDateMonth the month the web content article is set for
	 *         review
	 * @param  reviewDateDay the calendar day the web content article is set for
	 *         review
	 * @param  reviewDateYear the year the web content article is set for review
	 * @param  reviewDateHour the hour the web content article is set for review
	 * @param  reviewDateMinute the minute the web content article is set for
	 *         review
	 * @param  neverReview whether the web content article is not set for review
	 * @param  indexable whether the web content article is searchable
	 * @param  smallImage whether the web content article has a small image
	 * @param  smallImageURL the web content article's small image URL
	 * @param  smallImageFile the web content article's small image file
	 * @param  images the web content's images
	 * @param  articleURL the web content article's accessible URL
	 * @param  serviceContext the service context to be applied. Can set the
	 *         UUID, creation date, modification date, expando bridge
	 *         attributes, guest permissions, group permissions, asset category
	 *         IDs, asset tag names, asset link entry IDs, URL title, and
	 *         workflow actions for the web content article. Can also set
	 *         whether to add the default guest and group permissions.
	 * @return the web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle addArticle(
			long userId, long groupId, long folderId, long classNameId,
			long classPK, String articleId, boolean autoArticleId,
			double version, Map<Locale, String> titleMap,
			Map<Locale, String> descriptionMap, String content,
			String ddmStructureKey, String ddmTemplateKey, String layoutUuid,
			int displayDateMonth, int displayDateDay, int displayDateYear,
			int displayDateHour, int displayDateMinute, int expirationDateMonth,
			int expirationDateDay, int expirationDateYear,
			int expirationDateHour, int expirationDateMinute,
			boolean neverExpire, int reviewDateMonth, int reviewDateDay,
			int reviewDateYear, int reviewDateHour, int reviewDateMinute,
			boolean neverReview, boolean indexable, boolean smallImage,
			String smallImageURL, File smallImageFile,
			Map<String, byte[]> images, String articleURL,
			ServiceContext serviceContext)
		throws PortalException {

		return journalArticleLocalService.addArticle(
			userId, groupId, folderId, classNameId, classPK, articleId,
			autoArticleId, version, titleMap, descriptionMap, titleMap, content,
			ddmStructureKey, ddmTemplateKey, layoutUuid, displayDateMonth,
			displayDateDay, displayDateYear, displayDateHour, displayDateMinute,
			expirationDateMonth, expirationDateDay, expirationDateYear,
			expirationDateHour, expirationDateMinute, neverExpire,
			reviewDateMonth, reviewDateDay, reviewDateYear, reviewDateHour,
			reviewDateMinute, neverReview, indexable, smallImage, smallImageURL,
			smallImageFile, images, articleURL, serviceContext);
	}

	/**
	 * Adds a web content article.
	 *
	 * @param  userId the primary key of the web content article's creator/owner
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article folder
	 * @param  titleMap the web content article's locales and localized titles
	 * @param  descriptionMap the web content article's locales and localized
	 *         descriptions
	 * @param  content the HTML content wrapped in XML. For more information,
	 *         see the content example in the {@link #addArticle(long, long,
	 *         long, long, long, String, boolean, double, Map, Map, String,
	 *         String, String, String, int, int, int, int, int, int, int, int,
	 *         int, int, boolean, int, int, int, int, int, boolean, boolean,
	 *         boolean, String, File, Map, String, ServiceContext)} description.
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure, if the article is related to a DDM structure, or
	 *         <code>null</code> otherwise
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  serviceContext the service context to be applied. Can set the
	 *         UUID, creation date, modification date, expando bridge
	 *         attributes, guest permissions, group permissions, asset category
	 *         IDs, asset tag names, asset link entry IDs, asset priority, URL
	 *         title, and workflow actions for the web content article. Can also
	 *         set whether to add the default guest and group permissions.
	 * @return the web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle addArticle(
			long userId, long groupId, long folderId,
			Map<Locale, String> titleMap, Map<Locale, String> descriptionMap,
			String content, String ddmStructureKey, String ddmTemplateKey,
			ServiceContext serviceContext)
		throws PortalException {

		User user = userLocalService.getUser(userId);

		Calendar calendar = CalendarFactoryUtil.getCalendar(user.getTimeZone());

		int displayDateMonth = calendar.get(Calendar.MONTH);
		int displayDateDay = calendar.get(Calendar.DAY_OF_MONTH);
		int displayDateYear = calendar.get(Calendar.YEAR);
		int displayDateHour = calendar.get(Calendar.HOUR_OF_DAY);
		int displayDateMinute = calendar.get(Calendar.MINUTE);

		return journalArticleLocalService.addArticle(
			userId, groupId, folderId,
			JournalArticleConstants.CLASSNAME_ID_DEFAULT, 0, StringPool.BLANK,
			true, 1, titleMap, descriptionMap, content, ddmStructureKey,
			ddmTemplateKey, null, displayDateMonth, displayDateDay,
			displayDateYear, displayDateHour, displayDateMinute, 0, 0, 0, 0, 0,
			true, 0, 0, 0, 0, 0, true, true, false, null, null, null, null,
			serviceContext);
	}

	@Override
	public JournalArticle addArticleDefaultValues(
			long userId, long groupId, long classNameId, long classPK,
			Map<Locale, String> titleMap, Map<Locale, String> descriptionMap,
			String content, String ddmStructureKey, String ddmTemplateKey,
			String layoutUuid, boolean indexable, boolean smallImage,
			String smallImageURL, File smallImageFile,
			ServiceContext serviceContext)
		throws PortalException {

		// Article

		User user = userLocalService.getUser(userId);

		byte[] smallImageBytes = null;

		try {
			smallImageBytes = FileUtil.getBytes(smallImageFile);
		}
		catch (IOException ioe) {
		}

		String articleId = String.valueOf(counterLocalService.increment());

		sanitize(user.getCompanyId(), groupId, userId, classPK, descriptionMap);

		validate(
			user.getCompanyId(), groupId, classNameId, articleId, true, 0,
			titleMap, content, ddmStructureKey, ddmTemplateKey, null, null,
			smallImage, smallImageURL, smallImageFile, smallImageBytes,
			serviceContext);

		serviceContext.setAttribute("articleId", articleId);

		long id = counterLocalService.increment();

		JournalArticle article = journalArticlePersistence.create(id);

		article.setUuid(serviceContext.getUuid());

		String articleResourceUuid = GetterUtil.getString(
			serviceContext.getAttribute("articleResourceUuid"));

		long resourcePrimKey =
			_journalArticleResourceLocalService.getArticleResourcePrimKey(
				articleResourceUuid, groupId, articleId);

		article.setResourcePrimKey(resourcePrimKey);

		article.setGroupId(groupId);
		article.setCompanyId(user.getCompanyId());
		article.setUserId(user.getUserId());
		article.setUserName(user.getFullName());
		article.setClassNameId(classNameId);
		article.setClassPK(classPK);
		article.setArticleId(articleId);

		content = format(user, groupId, article, content);
		content = _replaceTempImages(article, content);

		article.setContent(content);

		article.setDDMStructureKey(ddmStructureKey);
		article.setDDMTemplateKey(ddmTemplateKey);

		Locale locale = getArticleDefaultLocale(content);

		article.setDefaultLanguageId(LocaleUtil.toLanguageId(locale));

		article.setLayoutUuid(layoutUuid);
		article.setIndexable(indexable);
		article.setSmallImage(smallImage);
		article.setSmallImageId(counterLocalService.increment());
		article.setSmallImageURL(smallImageURL);
		article.setStatus(WorkflowConstants.STATUS_APPROVED);
		article.setStatusByUserId(userId);
		article.setStatusDate(serviceContext.getModifiedDate(new Date()));
		article.setExpandoBridgeAttributes(serviceContext);

		journalArticlePersistence.update(article);

		// Article localization

		_addArticleLocalizedFields(
			user.getCompanyId(), article.getId(), titleMap, descriptionMap);

		// Resources

		addArticleResources(article, serviceContext.getModelPermissions());

		// Small image

		saveImages(
			smallImage, article.getSmallImageId(), smallImageFile,
			smallImageBytes);

		// Asset

		updateAsset(
			userId, article, serviceContext.getAssetCategoryIds(),
			serviceContext.getAssetTagNames(),
			serviceContext.getAssetLinkEntryIds(),
			serviceContext.getAssetPriority());

		// Dynamic data mapping

		updateDDMStructurePredefinedValues(classPK, content, serviceContext);

		return journalArticlePersistence.findByPrimaryKey(article.getId());
	}

	/**
	 * Adds the resources to the web content article.
	 *
	 * @param  article the web content article
	 * @param  addGroupPermissions whether to add group permissions
	 * @param  addGuestPermissions whether to add guest permissions
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void addArticleResources(
			JournalArticle article, boolean addGroupPermissions,
			boolean addGuestPermissions)
		throws PortalException {

		resourceLocalService.addResources(
			article.getCompanyId(), article.getGroupId(), article.getUserId(),
			JournalArticle.class.getName(), article.getResourcePrimKey(), false,
			addGroupPermissions, addGuestPermissions);
	}

	/**
	 * Adds the model resources with the permissions to the web content article.
	 *
	 * @param  article the web content article to add resources to
	 * @param  modelPermissions the model permissions
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void addArticleResources(
			JournalArticle article, ModelPermissions modelPermissions)
		throws PortalException {

		resourceLocalService.addModelResources(
			article.getCompanyId(), article.getGroupId(), article.getUserId(),
			JournalArticle.class.getName(), article.getResourcePrimKey(),
			modelPermissions);
	}

	/**
	 * Adds the model resources with the permissions to the web content article.
	 *
	 * @param      article the web content article to add resources to
	 * @param      groupPermissions the group permissions to be added
	 * @param      guestPermissions the guest permissions to be added
	 * @throws     PortalException if a portal exception occurred
	 * @deprecated As of Judson (7.1.x), replaced by {@link
	 *             #addArticleResources(JournalArticle, ModelPermissions)}
	 */
	@Deprecated
	@Override
	public void addArticleResources(
			JournalArticle article, String[] groupPermissions,
			String[] guestPermissions)
		throws PortalException {

		resourceLocalService.addModelResources(
			article.getCompanyId(), article.getGroupId(), article.getUserId(),
			JournalArticle.class.getName(), article.getResourcePrimKey(),
			groupPermissions, guestPermissions);
	}

	/**
	 * Adds the resources to the most recently created web content article.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  addGroupPermissions whether to add group permissions
	 * @param  addGuestPermissions whether to add guest permissions
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void addArticleResources(
			long groupId, String articleId, boolean addGroupPermissions,
			boolean addGuestPermissions)
		throws PortalException {

		JournalArticle article = getLatestArticle(groupId, articleId);

		addArticleResources(article, addGroupPermissions, addGuestPermissions);
	}

	/**
	 * Adds the resources with the permissions to the most recently created web
	 * content article.
	 *
	 * @param      groupId the primary key of the web content article's group
	 * @param      articleId the primary key of the web content article
	 * @param      groupPermissions the group permissions to be added
	 * @param      guestPermissions the guest permissions to be added
	 * @throws     PortalException if a portal exception occurred
	 * @deprecated As of Judson (7.1.x), replaced by {@link
	 *             #addArticleResources(JournalArticle, ModelPermissions)}
	 */
	@Deprecated
	@Override
	public void addArticleResources(
			long groupId, String articleId, String[] groupPermissions,
			String[] guestPermissions)
		throws PortalException {

		JournalArticle article = getLatestArticle(groupId, articleId);

		addArticleResources(article, groupPermissions, guestPermissions);
	}

	/**
	 * Returns the web content article with the group, article ID, and version.
	 * This method checks for the article's resource primary key and, if not
	 * found, creates a new one.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @return the matching web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle checkArticleResourcePrimKey(
			long groupId, String articleId, double version)
		throws PortalException {

		JournalArticle article = journalArticlePersistence.findByG_A_V(
			groupId, articleId, version);

		if (article.getResourcePrimKey() > 0) {
			return article;
		}

		long resourcePrimKey =
			_journalArticleResourceLocalService.getArticleResourcePrimKey(
				groupId, articleId);

		article.setResourcePrimKey(resourcePrimKey);

		journalArticlePersistence.update(article);

		return article;
	}

	/**
	 * Checks all web content articles by handling their expirations and sending
	 * review notifications based on their current workflow.
	 */
	@Override
	public void checkArticles() throws PortalException {
		Date now = new Date();

		checkArticlesByExpirationDate(now);

		checkArticlesByReviewDate(now);

		checkArticlesByDisplayDate(now);

		_previousCheckDate = now;
	}

	/**
	 * Checks the web content article matching the group, article ID, and
	 * version, replacing escaped newline and return characters with non-escaped
	 * newline and return characters.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void checkNewLine(long groupId, String articleId, double version)
		throws PortalException {

		JournalArticle article = journalArticlePersistence.findByG_A_V(
			groupId, articleId, version);

		String content = GetterUtil.getString(article.getContent());

		if (content.contains("\\n")) {
			content = StringUtil.replace(
				content, new String[] {"\\n", "\\r"},
				new String[] {"\n", "\r"});

			article.setContent(content);

			journalArticlePersistence.update(article);
		}
	}

	/**
	 * Checks the web content article matching the group, article ID, and
	 * version for an associated structure. If no structure is associated,
	 * return; otherwise check that the article and structure match.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void checkStructure(long groupId, String articleId, double version)
		throws PortalException {

		JournalArticle article = journalArticlePersistence.findByG_A_V(
			groupId, articleId, version);

		checkStructure(article);
	}

	/**
	 * Copies the web content article matching the group, article ID, and
	 * version. This method creates a new article, extracting all the values
	 * from the old one and updating its article ID.
	 *
	 * @param  userId the primary key of the web content article's creator/owner
	 * @param  groupId the primary key of the web content article's group
	 * @param  oldArticleId the primary key of the old web content article
	 * @param  newArticleId the primary key of the new web content article
	 * @param  autoArticleId whether to auto-generate the web content article ID
	 * @param  version the web content article's version
	 * @return the new web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle copyArticle(
			long userId, long groupId, String oldArticleId, String newArticleId,
			boolean autoArticleId, double version)
		throws PortalException {

		// Article

		User user = userLocalService.getUser(userId);
		oldArticleId = StringUtil.toUpperCase(StringUtil.trim(oldArticleId));
		newArticleId = StringUtil.toUpperCase(StringUtil.trim(newArticleId));

		JournalArticle oldArticle = journalArticlePersistence.findByG_A_V(
			groupId, oldArticleId, version);

		if (autoArticleId) {
			newArticleId = String.valueOf(counterLocalService.increment());
		}
		else {
			validate(newArticleId);

			if (journalArticlePersistence.countByG_A(groupId, newArticleId) >
					0) {

				StringBundler sb = new StringBundler(5);

				sb.append("{groupId=");
				sb.append(groupId);
				sb.append(", articleId=");
				sb.append(newArticleId);
				sb.append("}");

				throw new DuplicateArticleIdException(sb.toString());
			}
		}

		long id = counterLocalService.increment();

		long resourcePrimKey =
			_journalArticleResourceLocalService.getArticleResourcePrimKey(
				groupId, newArticleId);

		JournalArticle newArticle = journalArticlePersistence.create(id);

		newArticle.setResourcePrimKey(resourcePrimKey);
		newArticle.setGroupId(groupId);
		newArticle.setCompanyId(user.getCompanyId());
		newArticle.setUserId(user.getUserId());
		newArticle.setUserName(user.getFullName());
		newArticle.setFolderId(oldArticle.getFolderId());
		newArticle.setTreePath(oldArticle.getTreePath());
		newArticle.setArticleId(newArticleId);
		newArticle.setVersion(JournalArticleConstants.VERSION_DEFAULT);
		newArticle.setUrlTitle(
			getUniqueUrlTitle(
				id, groupId, newArticleId, oldArticle.getTitleCurrentValue()));

		try {
			copyArticleImages(oldArticle, newArticle);
		}
		catch (Exception e) {
			newArticle.setContent(oldArticle.getContent());
		}

		newArticle.setDDMStructureKey(oldArticle.getDDMStructureKey());
		newArticle.setDDMTemplateKey(oldArticle.getDDMTemplateKey());
		newArticle.setDefaultLanguageId(oldArticle.getDefaultLanguageId());
		newArticle.setLayoutUuid(oldArticle.getLayoutUuid());
		newArticle.setDisplayDate(oldArticle.getDisplayDate());
		newArticle.setExpirationDate(oldArticle.getExpirationDate());
		newArticle.setReviewDate(oldArticle.getReviewDate());
		newArticle.setIndexable(oldArticle.isIndexable());
		newArticle.setSmallImage(oldArticle.isSmallImage());
		newArticle.setSmallImageId(counterLocalService.increment());
		newArticle.setSmallImageURL(oldArticle.getSmallImageURL());

		WorkflowHandler workflowHandler =
			WorkflowHandlerRegistryUtil.getWorkflowHandler(
				JournalArticle.class.getName());

		WorkflowDefinitionLink workflowDefinitionLink =
			workflowHandler.getWorkflowDefinitionLink(
				oldArticle.getCompanyId(), oldArticle.getGroupId(),
				oldArticle.getId());

		if (oldArticle.isPending() || (workflowDefinitionLink != null)) {
			newArticle.setStatus(WorkflowConstants.STATUS_DRAFT);
		}
		else {
			newArticle.setStatus(oldArticle.getStatus());
		}

		ExpandoBridgeUtil.copyExpandoBridgeAttributes(
			oldArticle.getExpandoBridge(), newArticle.getExpandoBridge());

		journalArticlePersistence.update(newArticle);

		// Article localization

		String urlTitle = JournalUtil.getUrlTitle(id, oldArticle.getUrlTitle());

		int uniqueUrlTitleCount = _getUniqueUrlTitleCount(
			groupId, newArticleId, urlTitle);

		Map<Locale, String> newTitleMap = oldArticle.getTitleMap();

		for (Map.Entry<Locale, String> entry : newTitleMap.entrySet()) {
			Locale locale = entry.getKey();

			StringBundler sb = new StringBundler(5);

			sb.append(entry.getValue());
			sb.append(StringPool.SPACE);
			sb.append(LanguageUtil.get(locale, "duplicate"));
			sb.append(StringPool.SPACE);
			sb.append(uniqueUrlTitleCount);

			newTitleMap.put(locale, sb.toString());
		}

		_addArticleLocalizedFields(
			newArticle.getCompanyId(), newArticle.getId(), newTitleMap,
			oldArticle.getDescriptionMap());

		// Resources

		resourceLocalService.copyModelResources(
			oldArticle.getCompanyId(), JournalArticle.class.getName(),
			oldArticle.getResourcePrimKey(), resourcePrimKey);

		// Small image

		if (oldArticle.isSmallImage()) {
			Image image = imageLocalService.fetchImage(
				oldArticle.getSmallImageId());

			if (image != null) {
				byte[] smallImageBytes = image.getTextObj();

				imageLocalService.updateImage(
					newArticle.getSmallImageId(), smallImageBytes);
			}
		}

		// Asset

		long[] assetCategoryIds = assetCategoryLocalService.getCategoryIds(
			JournalArticle.class.getName(), oldArticle.getResourcePrimKey());
		String[] assetTagNames = assetTagLocalService.getTagNames(
			JournalArticle.class.getName(), oldArticle.getResourcePrimKey());

		AssetEntry oldAssetEntry = assetEntryLocalService.getEntry(
			JournalArticle.class.getName(), oldArticle.getResourcePrimKey());

		List<AssetLink> assetLinks = assetLinkLocalService.getDirectLinks(
			oldAssetEntry.getEntryId(), false);

		long[] assetLinkEntryIds = ListUtil.toLongArray(
			assetLinks, AssetLink.ENTRY_ID2_ACCESSOR);

		updateAsset(
			userId, newArticle, assetCategoryIds, assetTagNames,
			assetLinkEntryIds, oldAssetEntry.getPriority());

		// Dynamic data mapping

		updateDDMLinks(
			id, groupId, oldArticle.getDDMStructureKey(),
			oldArticle.getDDMTemplateKey(), true);

		return newArticle;
	}

	/**
	 * Deletes the web content article and its resources.
	 *
	 * @param  article the web content article
	 * @return the deleted web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	@SystemEvent(
		action = SystemEventConstants.ACTION_SKIP, send = false,
		type = SystemEventConstants.TYPE_DELETE
	)
	public JournalArticle deleteArticle(JournalArticle article)
		throws PortalException {

		return journalArticleLocalService.deleteArticle(
			article, StringPool.BLANK, null);
	}

	/**
	 * Deletes the web content article and its resources, optionally sending
	 * email notifying denial of the article if it had not yet been approved.
	 *
	 * @param  article the web content article
	 * @param  articleURL the web content article's accessible URL to include in
	 *         email notifications (optionally <code>null</code>)
	 * @param  serviceContext the service context to be applied (optionally
	 *         <code>null</code>). Can set the portlet preferences that include
	 *         email information to notify recipients of the unapproved web
	 *         content's denial.
	 * @return the deleted web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.DELETE)
	@Override
	@SystemEvent(
		action = SystemEventConstants.ACTION_SKIP, send = false,
		type = SystemEventConstants.TYPE_DELETE
	)
	public JournalArticle deleteArticle(
			JournalArticle article, String articleURL,
			ServiceContext serviceContext)
		throws PortalException {

		JournalArticleResource articleResource =
			_journalArticleResourceLocalService.fetchArticleResource(
				article.getGroupId(), article.getArticleId());

		if (article.isApproved() &&
			isLatestVersion(
				article.getGroupId(), article.getArticleId(),
				article.getVersion(), WorkflowConstants.STATUS_APPROVED)) {

			updatePreviousApprovedArticle(article);
		}

		// Article localization

		journalArticleLocalizationPersistence.removeByArticlePK(
			article.getId());

		// Asset

		if (article.isDraft() || article.isInTrash()) {
			assetEntryLocalService.deleteEntry(
				JournalArticle.class.getName(), article.getPrimaryKey());
		}

		// Email

		if ((serviceContext != null) && Validator.isNotNull(articleURL) &&
			!article.isApproved() &&
			isLatestVersion(
				article.getGroupId(), article.getArticleId(),
				article.getVersion())) {

			articleURL = buildArticleURL(
				articleURL, article.getGroupId(), article.getFolderId(),
				article.getArticleId());

			sendEmail(article, articleURL, "denied", serviceContext);
		}

		// Dynamic data mapping

		if (article.getClassNameId() != classNameLocalService.getClassNameId(
				DDMStructure.class)) {

			ddmStorageLinkLocalService.deleteClassStorageLink(article.getId());

			ddmStructureLinkLocalService.deleteStructureLinks(
				classNameLocalService.getClassNameId(JournalArticle.class),
				article.getId());

			ddmTemplateLinkLocalService.deleteTemplateLink(
				classNameLocalService.getClassNameId(JournalArticle.class),
				article.getId());
		}

		// Expando

		expandoRowLocalService.deleteRows(article.getId());

		// Trash

		if (article.isInTrash()) {
			TrashEntry trashEntry = article.getTrashEntry();

			if (trashEntry != null) {
				trashVersionLocalService.deleteTrashVersion(
					JournalArticle.class.getName(), article.getId());
			}
		}

		// Workflow

		if (!article.isDraft()) {
			workflowInstanceLinkLocalService.deleteWorkflowInstanceLink(
				article.getCompanyId(), article.getGroupId(),
				JournalArticle.class.getName(), article.getId());
		}

		int articlesCount = journalArticlePersistence.countByG_A(
			article.getGroupId(), article.getArticleId());

		if (articlesCount == 1) {

			// Asset

			assetEntryLocalService.deleteEntry(
				JournalArticle.class.getName(), article.getResourcePrimKey());

			// Comment

			_commentManager.deleteDiscussion(
				JournalArticle.class.getName(), article.getResourcePrimKey());

			// Content searches

			_journalContentSearchLocalService.deleteArticleContentSearches(
				article.getGroupId(), article.getArticleId());

			// Images

			long folderId = article.getImagesFolderId();

			if (folderId != DLFolderConstants.DEFAULT_PARENT_FOLDER_ID) {
				PortletFileRepositoryUtil.deletePortletFolder(folderId);
			}

			// Ratings

			ratingsStatsLocalService.deleteStats(
				JournalArticle.class.getName(), article.getResourcePrimKey());

			// Small image

			imageLocalService.deleteImage(article.getSmallImageId());

			// Trash

			trashEntryLocalService.deleteEntry(
				JournalArticle.class.getName(), article.getResourcePrimKey());

			// Resources

			resourceLocalService.deleteResource(
				article.getCompanyId(), JournalArticle.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				article.getResourcePrimKey());

			// Resource

			if (articleResource != null) {
				_journalArticleResourceLocalService.
					deleteJournalArticleResource(articleResource);
			}
		}

		// Friendly URL

		long classNameId = classNameLocalService.getClassNameId(
			JournalArticle.class);

		List<FriendlyURLEntry> friendlyURLEntries =
			friendlyURLEntryLocalService.getFriendlyURLEntries(
				article.getGroupId(), classNameId,
				article.getResourcePrimKey());

		if (!friendlyURLEntries.isEmpty()) {
			friendlyURLEntryLocalService.deleteFriendlyURLEntry(
				article.getGroupId(), JournalArticle.class,
				article.getResourcePrimKey());
		}

		// Article

		journalArticlePersistence.remove(article);

		// System event

		if (articleResource != null) {
			JSONObject extraDataJSONObject = JSONUtil.put(
				"uuid", article.getUuid()
			).put(
				"version", article.getVersion()
			);

			systemEventLocalService.addSystemEvent(
				0, article.getGroupId(), article.getModelClassName(),
				article.getPrimaryKey(), articleResource.getUuid(), null,
				SystemEventConstants.TYPE_DELETE,
				extraDataJSONObject.toString());
		}

		return article;
	}

	/**
	 * Deletes the web content article and its resources matching the group,
	 * article ID, and version, optionally sending email notifying denial of the
	 * web content article if it had not yet been approved.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @param  articleURL the web content article's accessible URL
	 * @param  serviceContext the service context to be applied. Can set the
	 *         portlet preferences that include email information to notify
	 *         recipients of the unapproved web content article's denial.
	 * @return the deleted web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle deleteArticle(
			long groupId, String articleId, double version, String articleURL,
			ServiceContext serviceContext)
		throws PortalException {

		JournalArticle article = journalArticlePersistence.findByG_A_V(
			groupId, articleId, version);

		return journalArticleLocalService.deleteArticle(
			article, articleURL, serviceContext);
	}

	/**
	 * Deletes all web content articles and their resources matching the group
	 * and article ID, optionally sending email notifying denial of article if
	 * it had not yet been approved.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  serviceContext the service context to be applied. Can set the
	 *         portlet preferences that include email information to notify
	 *         recipients of the unapproved web content article's denial.
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void deleteArticle(
			long groupId, String articleId, ServiceContext serviceContext)
		throws PortalException {

		SystemEventHierarchyEntryThreadLocal.push(JournalArticle.class);

		JournalArticleResource articleResource =
			_journalArticleResourceLocalService.fetchArticleResource(
				groupId, articleId);

		try {
			List<JournalArticle> articles = journalArticlePersistence.findByG_A(
				groupId, articleId, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
				new ArticleVersionComparator(true));

			for (JournalArticle article : articles) {
				journalArticleLocalService.deleteArticle(
					article, null, serviceContext);
			}
		}
		finally {
			SystemEventHierarchyEntryThreadLocal.pop(JournalArticle.class);
		}

		if (articleResource != null) {
			systemEventLocalService.addSystemEvent(
				0, groupId, JournalArticle.class.getName(),
				articleResource.getResourcePrimKey(), articleResource.getUuid(),
				null, SystemEventConstants.TYPE_DELETE, StringPool.BLANK);
		}
	}

	@Override
	public void deleteArticleDefaultValues(
			long groupId, String articleId, String ddmStructureKey)
		throws PortalException {

		// Dynamic data mapping

		_deleteDDMStructurePredefinedValues(groupId, ddmStructureKey);

		journalArticleLocalService.deleteArticle(groupId, articleId, null);
	}

	/**
	 * Deletes all the group's web content articles and resources.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void deleteArticles(long groupId) throws PortalException {
		SystemEventHierarchyEntryThreadLocal.push(JournalArticle.class);

		List<JournalArticleResource> articleResources = new ArrayList<>();

		try {
			JournalArticleResource articleResource = null;

			for (JournalArticle article :
					journalArticlePersistence.findByGroupId(groupId)) {

				if ((articleResource == null) ||
					(articleResource.getPrimaryKey() !=
						article.getResourcePrimKey())) {

					articleResource =
						_journalArticleResourceLocalService.getArticleResource(
							article.getResourcePrimKey());

					articleResources.add(articleResource);
				}

				journalArticleLocalService.deleteArticle(article, null, null);
			}
		}
		finally {
			SystemEventHierarchyEntryThreadLocal.pop(JournalArticle.class);
		}

		for (JournalArticleResource articleResource : articleResources) {
			systemEventLocalService.addSystemEvent(
				0, groupId, JournalArticle.class.getName(),
				articleResource.getResourcePrimKey(), articleResource.getUuid(),
				null, SystemEventConstants.TYPE_DELETE, StringPool.BLANK);
		}
	}

	/**
	 * Deletes all the group's web content articles and resources in the folder,
	 * including recycled articles.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article folder
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void deleteArticles(long groupId, long folderId)
		throws PortalException {

		deleteArticles(groupId, folderId, true);
	}

	/**
	 * Deletes all the group's web content articles and resources in the folder,
	 * optionally including recycled articles.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article folder
	 * @param  includeTrashedEntries whether to include recycled web content
	 *         articles
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void deleteArticles(
			long groupId, long folderId, boolean includeTrashedEntries)
		throws PortalException {

		SystemEventHierarchyEntryThreadLocal.push(JournalArticle.class);

		List<JournalArticleResource> articleResources = new ArrayList<>();

		try {
			JournalArticleResource articleResource = null;

			for (JournalArticle article :
					journalArticlePersistence.findByG_F(groupId, folderId)) {

				if ((articleResource == null) ||
					(articleResource.getPrimaryKey() !=
						article.getResourcePrimKey())) {

					articleResource =
						_journalArticleResourceLocalService.getArticleResource(
							article.getResourcePrimKey());

					articleResources.add(articleResource);
				}

				if (includeTrashedEntries || !article.isInTrashExplicitly()) {
					journalArticleLocalService.deleteArticle(
						article, null, null);
				}
				else {
					articleResources.remove(articleResource);
				}
			}
		}
		finally {
			SystemEventHierarchyEntryThreadLocal.pop(JournalArticle.class);
		}

		for (JournalArticleResource articleResource : articleResources) {
			systemEventLocalService.addSystemEvent(
				0, groupId, JournalArticle.class.getName(),
				articleResource.getResourcePrimKey(), articleResource.getUuid(),
				null, SystemEventConstants.TYPE_DELETE, StringPool.BLANK);
		}
	}

	/**
	 * Deletes all the group's web content articles and resources matching the
	 * class name and class primary key.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  className the DDMStructure class name if the web content article
	 *         is related to a DDM structure, the primary key of the class name
	 *         associated with the article, or
	 *         JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *         module otherwise
	 * @param  classPK the primary key of the DDM structure, if the DDMStructure
	 *         class name is given as the <code>className</code> parameter, the
	 *         primary key of the class associated with the web content article,
	 *         or <code>0</code> otherwise
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void deleteArticles(long groupId, String className, long classPK)
		throws PortalException {

		List<JournalArticle> articles = journalArticlePersistence.findByG_C_C(
			groupId, classNameLocalService.getClassNameId(className), classPK);

		for (JournalArticle article : articles) {
			journalArticleLocalService.deleteArticle(article, null, null);
		}
	}

	/**
	 * Deletes the layout's association with the web content articles for the
	 * group.
	 *
	 * @param groupId the primary key of the web content article's group
	 * @param layoutUuid the unique string identifying the web content article's
	 *        display page
	 */
	@Override
	public void deleteLayoutArticleReferences(long groupId, String layoutUuid) {
		List<JournalArticle> articles = journalArticlePersistence.findByG_L(
			groupId, layoutUuid);

		for (JournalArticle article : articles) {
			article.setLayoutUuid(StringPool.BLANK);

			journalArticlePersistence.update(article);
		}
	}

	/**
	 * Expires the web content article matching the group, article ID, and
	 * version.
	 *
	 * @param  userId the primary key of the user updating the web content
	 *         article
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @param  articleURL the web content article's accessible URL
	 * @param  serviceContext the service context to be applied. Can set the
	 *         modification date, status date, portlet preferences, and can set
	 *         whether to add the default command update for the web content
	 *         article. With respect to social activities, by setting the
	 *         service context's command to {@link Constants#UPDATE}, the
	 *         invocation is considered a web content update activity; otherwise
	 *         it is considered a web content add activity.
	 * @return the web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle expireArticle(
			long userId, long groupId, String articleId, double version,
			String articleURL, ServiceContext serviceContext)
		throws PortalException {

		return updateStatus(
			userId, groupId, articleId, version,
			WorkflowConstants.STATUS_EXPIRED, articleURL, new HashMap<>(),
			serviceContext);
	}

	/**
	 * Expires the web content article matching the group and article ID,
	 * expiring all of its versions if the
	 * <code>journal.article.expire.all.versions</code> portal property is
	 * <code>true</code>, otherwise expiring only its latest approved version.
	 *
	 * @param  userId the primary key of the user updating the web content
	 *         article
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  articleURL the web content article's accessible URL
	 * @param  serviceContext the service context to be applied. Can set the
	 *         modification date, status date, portlet preferences, and can set
	 *         whether to add the default command update for the web content
	 *         article. With respect to social activities, by setting the
	 *         service context's command to {@link Constants#UPDATE}, the
	 *         invocation is considered a web content update activity; otherwise
	 *         it is considered a web content add activity.
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void expireArticle(
			long userId, long groupId, String articleId, String articleURL,
			ServiceContext serviceContext)
		throws PortalException {

		User user = userLocalService.getUser(userId);

		if (isExpireAllArticleVersions(user.getCompanyId())) {
			List<JournalArticle> articles = journalArticlePersistence.findByG_A(
				groupId, articleId, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
				new ArticleVersionComparator(true));

			for (JournalArticle article : articles) {
				journalArticleLocalService.expireArticle(
					userId, groupId, article.getArticleId(),
					article.getVersion(), articleURL, serviceContext);
			}
		}
		else {
			JournalArticle article = getLatestArticle(
				groupId, articleId, WorkflowConstants.STATUS_APPROVED);

			journalArticleLocalService.expireArticle(
				userId, groupId, article.getArticleId(), article.getVersion(),
				articleURL, serviceContext);
		}
	}

	/**
	 * Returns the web content article with the ID.
	 *
	 * @param  id the primary key of the web content article
	 * @return the web content article with the ID
	 */
	@Override
	public JournalArticle fetchArticle(long id) {
		return journalArticlePersistence.fetchByPrimaryKey(id);
	}

	@Override
	public JournalArticle fetchArticle(long groupId, String articleId) {

		// Get the latest article that is approved, if none are approved, get
		// the latest unapproved article

		JournalArticle article = fetchLatestArticle(
			groupId, articleId, WorkflowConstants.STATUS_APPROVED);

		if (article != null) {
			return article;
		}

		return fetchLatestArticle(
			groupId, articleId, WorkflowConstants.STATUS_ANY);
	}

	/**
	 * Returns the web content article matching the group, article ID, and
	 * version.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @return the web content article matching the group, article ID, and
	 *         version, or <code>null</code> if no web content article could be
	 *         found
	 */
	@Override
	public JournalArticle fetchArticle(
		long groupId, String articleId, double version) {

		return journalArticlePersistence.fetchByG_A_V(
			groupId, articleId, version);
	}

	@Override
	public JournalArticle fetchArticleByUrlTitle(
		long groupId, String urlTitle) {

		JournalArticle article = fetchLatestArticleByUrlTitle(
			groupId, urlTitle, WorkflowConstants.STATUS_APPROVED);

		if (article != null) {
			return article;
		}

		return fetchLatestArticleByUrlTitle(
			groupId, urlTitle, WorkflowConstants.STATUS_ANY);
	}

	@Override
	public JournalArticle fetchArticleByUrlTitle(
		long groupId, String urlTitle, double version) {

		FriendlyURLEntry friendlyURLEntry =
			friendlyURLEntryLocalService.fetchFriendlyURLEntry(
				groupId, JournalArticle.class, urlTitle);

		if (friendlyURLEntry != null) {
			JournalArticle article = fetchLatestArticle(
				friendlyURLEntry.getClassPK(), WorkflowConstants.STATUS_ANY,
				true);

			if (article.getVersion() == version) {
				return article;
			}
		}

		JournalArticle article = fetchLatestArticleByUrlTitle(
			groupId, urlTitle, WorkflowConstants.STATUS_APPROVED);

		if ((article != null) && (article.getVersion() == version)) {
			return article;
		}

		article = fetchLatestArticleByUrlTitle(
			groupId, urlTitle, WorkflowConstants.STATUS_ANY);

		if ((article != null) && (article.getVersion() == version)) {
			return article;
		}
		else if (article != null) {
			return fetchArticle(
				article.getGroupId(), article.getArticleId(), version);
		}

		return null;
	}

	@Override
	public JournalArticle fetchDisplayArticle(long groupId, String articleId) {
		List<JournalArticle> articles = journalArticlePersistence.findByG_A_ST(
			groupId, articleId, WorkflowConstants.STATUS_APPROVED);

		if (articles.isEmpty()) {
			return null;
		}

		Date now = new Date();

		for (JournalArticle article : articles) {
			Date displayDate = article.getDisplayDate();
			Date expirationDate = article.getExpirationDate();

			if (((displayDate == null) || displayDate.before(now)) &&
				((expirationDate == null) || expirationDate.after(now))) {

				return article;
			}
		}

		return articles.get(0);
	}

	@Override
	public JournalArticle fetchLatestArticle(long resourcePrimKey) {
		return fetchLatestArticle(
			resourcePrimKey, WorkflowConstants.STATUS_ANY);
	}

	@Override
	public JournalArticle fetchLatestArticle(long resourcePrimKey, int status) {
		return fetchLatestArticle(resourcePrimKey, status, true);
	}

	/**
	 * Returns the latest web content article matching the resource primary key
	 * and workflow status, optionally preferring articles with approved
	 * workflow status.
	 *
	 * @param  resourcePrimKey the primary key of the resource instance
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  preferApproved whether to prefer returning the latest matching
	 *         article that has workflow status {@link
	 *         WorkflowConstants#STATUS_APPROVED} over returning one that has a
	 *         different status
	 * @return the latest web content article matching the resource primary key
	 *         and workflow status, optionally preferring articles with an
	 *         approved workflow status, or <code>null</code> if no matching web
	 *         content article could be found
	 */
	@Override
	public JournalArticle fetchLatestArticle(
		long resourcePrimKey, int status, boolean preferApproved) {

		JournalArticle article = null;

		OrderByComparator<JournalArticle> orderByComparator =
			new ArticleVersionComparator();

		if (status == WorkflowConstants.STATUS_ANY) {
			if (preferApproved) {
				article = journalArticlePersistence.fetchByR_ST_First(
					resourcePrimKey, WorkflowConstants.STATUS_APPROVED,
					orderByComparator);
			}

			if (article == null) {
				article =
					journalArticlePersistence.fetchByResourcePrimKey_First(
						resourcePrimKey, orderByComparator);
			}
		}
		else {
			article = journalArticlePersistence.fetchByR_ST_First(
				resourcePrimKey, status, orderByComparator);
		}

		return article;
	}

	@Override
	public JournalArticle fetchLatestArticle(
		long resourcePrimKey, int[] statuses) {

		OrderByComparator<JournalArticle> orderByComparator =
			new ArticleVersionComparator();

		List<JournalArticle> articles = journalArticlePersistence.findByR_ST(
			resourcePrimKey, statuses, 0, 1, orderByComparator);

		if (!articles.isEmpty()) {
			return articles.get(0);
		}

		return null;
	}

	/**
	 * Returns the latest web content article matching the group, article ID,
	 * and workflow status.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @return the latest matching web content article, or <code>null</code> if
	 *         no matching web content article could be found
	 */
	@Override
	public JournalArticle fetchLatestArticle(
		long groupId, String articleId, int status) {

		OrderByComparator<JournalArticle> orderByComparator =
			new ArticleVersionComparator();

		if (status == WorkflowConstants.STATUS_ANY) {
			return journalArticlePersistence.fetchByG_A_NotST_First(
				groupId, articleId, WorkflowConstants.STATUS_IN_TRASH,
				orderByComparator);
		}

		return journalArticlePersistence.fetchByG_A_ST_First(
			groupId, articleId, status, orderByComparator);
	}

	@Override
	public JournalArticle fetchLatestArticleByUrlTitle(
		long groupId, String urlTitle, int status) {

		FriendlyURLEntry friendlyURLEntry =
			friendlyURLEntryLocalService.fetchFriendlyURLEntry(
				groupId, JournalArticle.class, urlTitle);

		if (friendlyURLEntry != null) {
			JournalArticle article = fetchLatestArticle(
				friendlyURLEntry.getClassPK(), status);

			if ((article != null) && (article.getGroupId() != groupId)) {
				article = fetchLatestArticle(
					groupId, article.getArticleId(), status);
			}

			return article;
		}

		List<JournalArticle> articles = null;

		OrderByComparator<JournalArticle> orderByComparator =
			new ArticleVersionComparator();

		if (status == WorkflowConstants.STATUS_ANY) {
			articles = journalArticlePersistence.findByG_UT(
				groupId,
				FriendlyURLNormalizerUtil.normalizeWithEncoding(urlTitle), 0, 1,
				orderByComparator);
		}
		else {
			articles = journalArticlePersistence.findByG_UT_ST(
				groupId,
				FriendlyURLNormalizerUtil.normalizeWithEncoding(urlTitle),
				status, 0, 1, orderByComparator);
		}

		if (articles.isEmpty()) {
			return null;
		}

		return articles.get(0);
	}

	/**
	 * Returns the latest indexable web content article matching the resource
	 * primary key.
	 *
	 * @param  resourcePrimKey the primary key of the resource instance
	 * @return the latest indexable web content article matching the resource
	 *         primary key, or <code>null</code> if no matching web content
	 *         article could be found
	 */
	@Override
	public JournalArticle fetchLatestIndexableArticle(long resourcePrimKey) {
		OrderByComparator<JournalArticle> orderByComparator =
			new ArticleVersionComparator();

		int[] statuses = {
			WorkflowConstants.STATUS_APPROVED, WorkflowConstants.STATUS_IN_TRASH
		};

		List<JournalArticle> articles = journalArticlePersistence.findByR_I_S(
			resourcePrimKey, true, statuses, 0, 1, orderByComparator);

		if (articles.isEmpty()) {
			return null;
		}

		return articles.get(0);
	}

	/**
	 * Returns the web content article with the ID.
	 *
	 * @param  id the primary key of the web content article
	 * @return the web content article with the ID
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getArticle(long id) throws PortalException {
		return journalArticlePersistence.findByPrimaryKey(id);
	}

	/**
	 * Returns the latest approved web content article, or the latest unapproved
	 * article if none are approved. Both approved and unapproved articles must
	 * match the group and article ID.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @return the matching web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getArticle(long groupId, String articleId)
		throws PortalException {

		// Get the latest article that is approved, if none are approved, get
		// the latest unapproved article

		JournalArticle article = fetchLatestArticle(
			groupId, articleId, WorkflowConstants.STATUS_APPROVED);

		if (article != null) {
			return article;
		}

		return getLatestArticle(
			groupId, articleId, WorkflowConstants.STATUS_ANY);
	}

	/**
	 * Returns the web content article matching the group, article ID, and
	 * version.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @return the matching web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getArticle(
			long groupId, String articleId, double version)
		throws PortalException {

		return journalArticlePersistence.findByG_A_V(
			groupId, articleId, version);
	}

	/**
	 * Returns the web content article matching the group, class name, and class
	 * PK.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  className the DDMStructure class name if the web content article
	 *         is related to a DDM structure, the primary key of the class name
	 *         associated with the article, or
	 *         JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *         module otherwise
	 * @param  classPK the primary key of the DDM structure, if the DDMStructure
	 *         class name is given as the <code>className</code> parameter, the
	 *         primary key of the class associated with the web content article,
	 *         or <code>0</code> otherwise
	 * @return the matching web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getArticle(
			long groupId, String className, long classPK)
		throws PortalException {

		List<JournalArticle> articles = journalArticlePersistence.findByG_C_C(
			groupId, classNameLocalService.getClassNameId(className), classPK);

		if (articles.isEmpty()) {
			throw new NoSuchArticleException(
				StringBundler.concat(
					"No approved JournalArticle exists with the key {groupId=",
					groupId, ", className=", className, ", classPK=", classPK,
					"}"));
		}

		return articles.get(0);
	}

	/**
	 * Returns the latest web content article that is approved, or the latest
	 * unapproved article if none are approved. Both approved and unapproved
	 * articles must match the group and URL title.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  urlTitle the web content article's accessible URL title
	 * @return the matching web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getArticleByUrlTitle(long groupId, String urlTitle)
		throws PortalException {

		FriendlyURLEntry friendlyURLEntry =
			friendlyURLEntryLocalService.fetchFriendlyURLEntry(
				groupId, JournalArticle.class, urlTitle);

		if (friendlyURLEntry != null) {
			return getLatestArticle(
				friendlyURLEntry.getClassPK(), WorkflowConstants.STATUS_ANY,
				true);
		}

		// Get the latest article that is approved, if none are approved, get
		// the latest unapproved article

		JournalArticle article = fetchLatestArticleByUrlTitle(
			groupId, urlTitle, WorkflowConstants.STATUS_APPROVED);

		if (article != null) {
			return article;
		}

		return getLatestArticleByUrlTitle(
			groupId, urlTitle, WorkflowConstants.STATUS_ANY);
	}

	/**
	 * Returns the web content from the web content article associated with the
	 * portlet request model and the DDM template.
	 *
	 * @param  article the web content article
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  viewMode the mode in which the web content is being viewed
	 * @param  languageId the primary key of the language translation to get
	 * @param  portletRequestModel the portlet request model
	 * @param  themeDisplay the theme display
	 * @return the web content from the web content article associated with the
	 *         portlet request model and the DDM template
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public String getArticleContent(
			JournalArticle article, String ddmTemplateKey, String viewMode,
			String languageId, PortletRequestModel portletRequestModel,
			ThemeDisplay themeDisplay)
		throws PortalException {

		JournalArticleDisplay articleDisplay = getArticleDisplay(
			article, ddmTemplateKey, viewMode, languageId, 1,
			portletRequestModel, themeDisplay);

		if (articleDisplay == null) {
			return StringPool.BLANK;
		}

		return articleDisplay.getContent();
	}

	/**
	 * Returns the web content from the web content article associated with the
	 * DDM template.
	 *
	 * @param      article the web content article
	 * @param      ddmTemplateKey the primary key of the web content article's
	 *             DDM template
	 * @param      viewMode the mode in which the web content is being viewed
	 * @param      languageId the primary key of the language translation to get
	 * @param      themeDisplay the theme display
	 * @return     the web content from the matching web content article
	 * @throws     PortalException if a portal exception occurred
	 * @deprecated As of Wilberforce (7.0.x), replaced by {@link
	 *             #getArticleContent(JournalArticle, String, String, String,
	 *             PortletRequestModel,ThemeDisplay)}
	 */
	@Deprecated
	@Override
	public String getArticleContent(
			JournalArticle article, String ddmTemplateKey, String viewMode,
			String languageId, ThemeDisplay themeDisplay)
		throws PortalException {

		return getArticleContent(
			article, ddmTemplateKey, viewMode, languageId, null, themeDisplay);
	}

	/**
	 * Returns the web content from the web content article matching the group,
	 * article ID, and version, and associated with the portlet request model
	 * and the DDM template.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @param  viewMode the mode in which the web content is being viewed
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  languageId the primary key of the language translation to get
	 * @param  portletRequestModel the portlet request model
	 * @param  themeDisplay the theme display
	 * @return the web content from the matching web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public String getArticleContent(
			long groupId, String articleId, double version, String viewMode,
			String ddmTemplateKey, String languageId,
			PortletRequestModel portletRequestModel, ThemeDisplay themeDisplay)
		throws PortalException {

		JournalArticleDisplay articleDisplay = getArticleDisplay(
			groupId, articleId, version, ddmTemplateKey, viewMode, languageId,
			1, portletRequestModel, themeDisplay);

		if (articleDisplay == null) {
			return StringPool.BLANK;
		}

		return articleDisplay.getContent();
	}

	/**
	 * Returns the web content from the web content article matching the group,
	 * article ID, and version, and associated with the DDM template.
	 *
	 * @param      groupId the primary key of the web content article's group
	 * @param      articleId the primary key of the web content article
	 * @param      version the web content article's version
	 * @param      viewMode the mode in which the web content is being viewed
	 * @param      ddmTemplateKey the primary key of the web content article's
	 *             DDM template (optionally <code>null</code>). If the article
	 *             is related to a DDM structure, the template's structure must
	 *             match it.
	 * @param      languageId the primary key of the language translation to get
	 * @param      themeDisplay the theme display
	 * @return     the web content from the matching web content article
	 * @throws     PortalException if a portal exception occurred
	 * @deprecated As of Wilberforce (7.0.x), replaced by {@link
	 *             #getArticleContent(long, String, double, String, String,
	 *             String, PortletRequestModel, ThemeDisplay)}
	 */
	@Deprecated
	@Override
	public String getArticleContent(
			long groupId, String articleId, double version, String viewMode,
			String ddmTemplateKey, String languageId, ThemeDisplay themeDisplay)
		throws PortalException {

		return getArticleContent(
			groupId, articleId, version, viewMode, ddmTemplateKey, languageId,
			null, themeDisplay);
	}

	/**
	 * Returns the web content from the web content article matching the group,
	 * article ID, and version.
	 *
	 * @param      groupId the primary key of the web content article's group
	 * @param      articleId the primary key of the web content article
	 * @param      version the web content article's version
	 * @param      viewMode the mode in which the web content is being viewed
	 * @param      languageId the primary key of the language translation to get
	 * @param      themeDisplay the theme display
	 * @return     the web content from the matching web content article
	 * @throws     PortalException if a portal exception occurred
	 * @deprecated As of Wilberforce (7.0.x), replaced by {@link
	 *             #getArticleContent(long, String, double, String, String,
	 *             String, PortletRequestModel, ThemeDisplay)}
	 */
	@Deprecated
	@Override
	public String getArticleContent(
			long groupId, String articleId, double version, String viewMode,
			String languageId, ThemeDisplay themeDisplay)
		throws PortalException {

		return getArticleContent(
			groupId, articleId, version, viewMode, null, languageId, null,
			themeDisplay);
	}

	/**
	 * Returns the latest web content from the web content article matching the
	 * group and article ID, and associated with the portlet request model and
	 * the DDM template.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  viewMode the mode in which the web content is being viewed
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  languageId the primary key of the language translation to get
	 * @param  portletRequestModel the portlet request model
	 * @param  themeDisplay the theme display
	 * @return the latest web content from the matching web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public String getArticleContent(
			long groupId, String articleId, String viewMode,
			String ddmTemplateKey, String languageId,
			PortletRequestModel portletRequestModel, ThemeDisplay themeDisplay)
		throws PortalException {

		JournalArticleDisplay articleDisplay = getArticleDisplay(
			groupId, articleId, ddmTemplateKey, viewMode, languageId, 1,
			portletRequestModel, themeDisplay);

		return articleDisplay.getContent();
	}

	/**
	 * Returns the latest web content from the web content article matching the
	 * group and article ID, and associated with the DDM template.
	 *
	 * @param      groupId the primary key of the web content article's group
	 * @param      articleId the primary key of the web content article
	 * @param      viewMode the mode in which the web content is being viewed
	 * @param      ddmTemplateKey the primary key of the web content article's
	 *             DDM template
	 * @param      languageId the primary key of the language translation to get
	 * @param      themeDisplay the theme display
	 * @return     the latest web content from the matching web content article
	 * @throws     PortalException if a portal exception occurred
	 * @deprecated As of Wilberforce (7.0.x), replaced by {@link
	 *             #getArticleContent(long, String, String, String, String,
	 *             PortletRequestModel, ThemeDisplay)}
	 */
	@Deprecated
	@Override
	public String getArticleContent(
			long groupId, String articleId, String viewMode,
			String ddmTemplateKey, String languageId, ThemeDisplay themeDisplay)
		throws PortalException {

		return getArticleContent(
			groupId, articleId, viewMode, ddmTemplateKey, languageId, null,
			themeDisplay);
	}

	/**
	 * Returns the latest web content from the web content article matching the
	 * group and article ID.
	 *
	 * @param      groupId the primary key of the web content article's group
	 * @param      articleId the primary key of the web content article
	 * @param      viewMode the mode in which the web content is being viewed
	 * @param      languageId the primary key of the language translation to get
	 * @param      themeDisplay the theme display
	 * @return     the latest web content from the matching web content article
	 * @throws     PortalException if a portal exception occurred
	 * @deprecated As of Wilberforce (7.0.x), replaced by {@link
	 *             #getArticleContent(long, String, String, String, String,
	 *             PortletRequestModel, ThemeDisplay)}
	 */
	@Deprecated
	@Override
	public String getArticleContent(
			long groupId, String articleId, String viewMode, String languageId,
			ThemeDisplay themeDisplay)
		throws PortalException {

		JournalArticle article = fetchDisplayArticle(groupId, articleId);

		return getArticleContent(
			groupId, articleId, viewMode, article.getDDMTemplateKey(),
			languageId, null, themeDisplay);
	}

	@Override
	public String getArticleDescription(long articlePK, Locale locale) {
		String languageId = LocaleUtil.toLanguageId(locale);

		return getArticleDescription(articlePK, languageId);
	}

	@Override
	public String getArticleDescription(long articlePK, String languageId) {
		JournalArticleLocalization journalArticleLocalization =
			journalArticleLocalizationPersistence.fetchByA_L(
				articlePK, languageId);

		if (journalArticleLocalization == null) {
			return null;
		}

		return journalArticleLocalization.getDescription();
	}

	@Override
	public Map<Locale, String> getArticleDescriptionMap(long articlePK) {
		Map<Locale, String> journalArticleLocalizationDescriptionMap =
			new HashMap<>();

		List<JournalArticleLocalization> journalArticleLocalizationList =
			journalArticleLocalizationPersistence.findByArticlePK(articlePK);

		for (JournalArticleLocalization journalArticleLocalization :
				journalArticleLocalizationList) {

			journalArticleLocalizationDescriptionMap.put(
				LocaleUtil.fromLanguageId(
					journalArticleLocalization.getLanguageId()),
				journalArticleLocalization.getDescription());
		}

		return journalArticleLocalizationDescriptionMap;
	}

	/**
	 * Returns a web content article display for the specified page of the
	 * latest version of the web content article, based on the DDM template. Web
	 * content transformation tokens are added using the portlet request model
	 * and theme display.
	 *
	 * @param  article the primary key of the web content article
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  viewMode the mode in which the web content is being viewed
	 * @param  languageId the primary key of the language translation to get
	 * @param  page the web content article page to display
	 * @param  portletRequestModel the portlet request model
	 * @param  themeDisplay the theme display
	 * @return the web content article display, or <code>null</code> if the
	 *         article has expired or if article's display date/time is after
	 *         the current date/time
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticleDisplay getArticleDisplay(
			JournalArticle article, String ddmTemplateKey, String viewMode,
			String languageId, int page,
			PortletRequestModel portletRequestModel, ThemeDisplay themeDisplay)
		throws PortalException {

		return getArticleDisplay(
			article, ddmTemplateKey, viewMode, languageId, page,
			portletRequestModel, themeDisplay, false);
	}

	/**
	 * Returns a web content article display for the specified page of the
	 * specified version of the web content article matching the group, article
	 * ID, and DDM template. Web content transformation tokens are added using
	 * the portlet request model and theme display.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  viewMode the mode in which the web content is being viewed
	 * @param  languageId the primary key of the language translation to get
	 * @param  page the web content article page to display
	 * @param  portletRequestModel the portlet request model
	 * @param  themeDisplay the theme display
	 * @return the web content article display, or <code>null</code> if the
	 *         article has expired or if article's display date/time is after
	 *         the current date/time
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticleDisplay getArticleDisplay(
			long groupId, String articleId, double version,
			String ddmTemplateKey, String viewMode, String languageId, int page,
			PortletRequestModel portletRequestModel, ThemeDisplay themeDisplay)
		throws PortalException {

		Date now = new Date();

		JournalArticle article = journalArticlePersistence.findByG_A_V(
			groupId, articleId, version);

		if (article.isExpired()) {
			Date expirationDate = article.getExpirationDate();

			if ((expirationDate != null) && expirationDate.before(now)) {
				return null;
			}
		}

		Date displayDate = article.getDisplayDate();

		if ((displayDate != null) && displayDate.after(now)) {
			return null;
		}

		return getArticleDisplay(
			article, ddmTemplateKey, viewMode, languageId, page,
			portletRequestModel, themeDisplay);
	}

	/**
	 * Returns a web content article display for the first page of the specified
	 * version of the web content article matching the group, article ID, and
	 * DDM template. Web content transformation tokens are added from the theme
	 * display (if not <code>null</code>).
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  viewMode the mode in which the web content is being viewed
	 * @param  languageId the primary key of the language translation to get
	 * @param  themeDisplay the theme display
	 * @return the web content article display, or <code>null</code> if the
	 *         article has expired or if article's display date/time is after
	 *         the current date/time
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticleDisplay getArticleDisplay(
			long groupId, String articleId, double version,
			String ddmTemplateKey, String viewMode, String languageId,
			ThemeDisplay themeDisplay)
		throws PortalException {

		return getArticleDisplay(
			groupId, articleId, version, ddmTemplateKey, viewMode, languageId,
			1, null, themeDisplay);
	}

	/**
	 * Returns a web content article display for the specified page of the
	 * latest version of the web content article matching the group and article
	 * ID. Web content transformation tokens are added from the theme display
	 * (if not <code>null</code>).
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  viewMode the mode in which the web content is being viewed
	 * @param  languageId the primary key of the language translation to get
	 * @param  page the web content article page to display
	 * @param  portletRequestModel the portlet request model
	 * @param  themeDisplay the theme display
	 * @return the web content article display, or <code>null</code> if the
	 *         article has expired or if article's display date/time is after
	 *         the current date/time
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticleDisplay getArticleDisplay(
			long groupId, String articleId, String viewMode, String languageId,
			int page, PortletRequestModel portletRequestModel,
			ThemeDisplay themeDisplay)
		throws PortalException {

		JournalArticle article = getDisplayArticle(groupId, articleId);

		return getArticleDisplay(
			groupId, articleId, article.getDDMTemplateKey(), viewMode,
			languageId, page, portletRequestModel, themeDisplay);
	}

	/**
	 * Returns a web content article display for the specified page of the
	 * latest version of the web content article matching the group, article ID,
	 * and DDM template. Web content transformation tokens are added using the
	 * portlet request model and theme display.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  viewMode the mode in which the web content is being viewed
	 * @param  languageId the primary key of the language translation to get
	 * @param  page the web content article page to display
	 * @param  portletRequestModel the portlet request model
	 * @param  themeDisplay the theme display
	 * @return the web content article display, or <code>null</code> if the
	 *         article has expired or if article's display date/time is after
	 *         the current date/time
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticleDisplay getArticleDisplay(
			long groupId, String articleId, String ddmTemplateKey,
			String viewMode, String languageId, int page,
			PortletRequestModel portletRequestModel, ThemeDisplay themeDisplay)
		throws PortalException {

		JournalArticle article = getDisplayArticle(groupId, articleId);

		return getArticleDisplay(
			groupId, articleId, article.getVersion(), ddmTemplateKey, viewMode,
			languageId, page, portletRequestModel, themeDisplay);
	}

	/**
	 * Returns a web content article display for the first page of the latest
	 * version of the web content article matching the group, article ID, and
	 * DDM template. Web content transformation tokens are added from the theme
	 * display (if not <code>null</code>).
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  viewMode the mode in which the web content is being viewed
	 * @param  languageId the primary key of the language translation to get
	 * @param  themeDisplay the theme display
	 * @return the web content article display, or <code>null</code> if the
	 *         article has expired or if article's display date/time is after
	 *         the current date/time
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticleDisplay getArticleDisplay(
			long groupId, String articleId, String ddmTemplateKey,
			String viewMode, String languageId, ThemeDisplay themeDisplay)
		throws PortalException {

		JournalArticle article = getDisplayArticle(groupId, articleId);

		return getArticleDisplay(
			groupId, articleId, article.getVersion(), ddmTemplateKey, viewMode,
			languageId, themeDisplay);
	}

	/**
	 * Returns a web content article display for the first page of the latest
	 * version of the web content article matching the group and article ID. Web
	 * content transformation tokens are added from the theme display (if not
	 * <code>null</code>).
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  viewMode the mode in which the web content is being viewed
	 * @param  languageId the primary key of the language translation to get
	 * @param  themeDisplay the theme display
	 * @return the web content article display, or <code>null</code> if the
	 *         article has expired or if article's display date/time is after
	 *         the current date/time
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticleDisplay getArticleDisplay(
			long groupId, String articleId, String viewMode, String languageId,
			ThemeDisplay themeDisplay)
		throws PortalException {

		JournalArticle article = getDisplayArticle(groupId, articleId);

		return getArticleDisplay(
			groupId, articleId, article.getDDMTemplateKey(), viewMode,
			languageId, themeDisplay);
	}

	@Override
	public List<String> getArticleLocalizationLanguageIds(long articlePK) {
		List<JournalArticleLocalization> journalArticleLocalizationList =
			journalArticleLocalizationPersistence.findByArticlePK(articlePK);

		List<String> availableLanguageIds = new ArrayList<>();

		for (JournalArticleLocalization journalArticleLocalization :
				journalArticleLocalizationList) {

			availableLanguageIds.add(
				journalArticleLocalization.getLanguageId());
		}

		return availableLanguageIds;
	}

	/**
	 * Returns all the web content articles present in the system.
	 *
	 * @return the web content articles present in the system
	 */
	@Override
	public List<JournalArticle> getArticles() {
		return journalArticlePersistence.findAll();
	}

	/**
	 * Returns all the web content articles belonging to the group.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @return the web content articles belonging to the group
	 */
	@Override
	public List<JournalArticle> getArticles(long groupId) {
		return journalArticlePersistence.findByGroupId(groupId);
	}

	/**
	 * Returns a range of all the web content articles belonging to the group.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @return the range of matching web content articles
	 */
	@Override
	public List<JournalArticle> getArticles(long groupId, int start, int end) {
		return journalArticlePersistence.findByGroupId(groupId, start, end);
	}

	/**
	 * Returns an ordered range of all the web content articles belonging to the
	 * group.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @param  obc the comparator to order the web content articles
	 * @return the range of matching web content articles ordered by the
	 *         comparator
	 */
	@Override
	public List<JournalArticle> getArticles(
		long groupId, int start, int end,
		OrderByComparator<JournalArticle> obc) {

		return journalArticlePersistence.findByGroupId(
			groupId, start, end, obc);
	}

	/**
	 * Returns all the web content articles matching the group and folder.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article folder
	 * @return the matching web content articles
	 */
	@Override
	public List<JournalArticle> getArticles(long groupId, long folderId) {
		return journalArticlePersistence.findByG_F(groupId, folderId);
	}

	/**
	 * Returns a range of all the web content articles matching the group and
	 * folder.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article's folder
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @return the range of matching web content articles
	 */
	@Override
	public List<JournalArticle> getArticles(
		long groupId, long folderId, int start, int end) {

		return journalArticlePersistence.findByG_F(
			groupId, folderId, start, end);
	}

	/**
	 * Returns a range of all the web content articles matching the group,
	 * folder, and status.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article's folder
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @return the range of matching web content articles
	 */
	@Override
	public List<JournalArticle> getArticles(
		long groupId, long folderId, int status, int start, int end) {

		return journalArticlePersistence.findByG_F_ST(
			groupId, folderId, status, start, end);
	}

	/**
	 * Returns an ordered range of all the web content articles matching the
	 * group and folder.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article's folder
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @param  orderByComparator the comparator to order the web content
	 *         articles
	 * @return the range of matching web content articles ordered by the
	 *         comparator
	 */
	@Override
	public List<JournalArticle> getArticles(
		long groupId, long folderId, int start, int end,
		OrderByComparator<JournalArticle> orderByComparator) {

		return journalArticlePersistence.findByG_F(
			groupId, folderId, start, end, orderByComparator);
	}

	/**
	 * Returns all the web content articles matching the group and article ID.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @return the matching web content articles
	 */
	@Override
	public List<JournalArticle> getArticles(long groupId, String articleId) {
		return journalArticlePersistence.findByG_A(groupId, articleId);
	}

	@Override
	public List<JournalArticle> getArticles(
		long groupId, String articleId, int start, int end,
		OrderByComparator<JournalArticle> orderByComparator) {

		return journalArticlePersistence.findByG_A(
			groupId, articleId, start, end, orderByComparator);
	}

	/**
	 * Returns all the web content articles matching the resource primary key.
	 *
	 * @param  resourcePrimKey the primary key of the resource instance
	 * @return the web content articles matching the resource primary key
	 */
	@Override
	public List<JournalArticle> getArticlesByResourcePrimKey(
		long resourcePrimKey) {

		return journalArticlePersistence.findByResourcePrimKey(resourcePrimKey);
	}

	/**
	 * Returns all the web content articles matching the small image ID.
	 *
	 * @param  smallImageId the primary key of the web content article's small
	 *         image
	 * @return the web content articles matching the small image ID
	 */
	@Override
	public List<JournalArticle> getArticlesBySmallImageId(long smallImageId) {
		return journalArticlePersistence.findBySmallImageId(smallImageId);
	}

	@Override
	public List<JournalArticle> getArticlesByStructureId(
		long groupId, long classNameId, String ddmStructureKey, int status,
		int start, int end, OrderByComparator<JournalArticle> obc) {

		QueryDefinition<JournalArticle> queryDefinition = new QueryDefinition<>(
			status, start, end, obc);

		return journalArticleFinder.findByG_C_S_L(
			groupId, classNameId, ddmStructureKey,
			LocaleUtil.getMostRelevantLocale(), queryDefinition);
	}

	@Override
	public List<JournalArticle> getArticlesByStructureId(
		long groupId, long classNameId, String ddmStructureKey, Locale locale,
		int status, int start, int end, OrderByComparator<JournalArticle> obc) {

		QueryDefinition<JournalArticle> queryDefinition = new QueryDefinition<>(
			status, start, end, obc);

		return journalArticleFinder.findByG_C_S_L(
			groupId, classNameId, ddmStructureKey, locale, queryDefinition);
	}

	@Override
	public List<JournalArticle> getArticlesByStructureId(
		long groupId, String ddmStructureKey, int status, int start, int end,
		OrderByComparator<JournalArticle> obc) {

		QueryDefinition<JournalArticle> queryDefinition = new QueryDefinition<>(
			status, start, end, obc);

		return journalArticleFinder.findByG_C_S_L(
			groupId, JournalArticleConstants.CLASSNAME_ID_DEFAULT,
			ddmStructureKey, LocaleUtil.getMostRelevantLocale(),
			queryDefinition);
	}

	@Override
	public List<JournalArticle> getArticlesByStructureId(
		long groupId, String ddmStructureKey, int start, int end,
		OrderByComparator<JournalArticle> obc) {

		return getArticlesByStructureId(
			groupId, ddmStructureKey, WorkflowConstants.STATUS_ANY, start, end,
			obc);
	}

	@Override
	public List<JournalArticle> getArticlesByStructureId(
		long groupId, String ddmStructureKey, Locale locale, int status,
		int start, int end, OrderByComparator<JournalArticle> obc) {

		QueryDefinition<JournalArticle> queryDefinition = new QueryDefinition<>(
			status, start, end, obc);

		return journalArticleFinder.findByG_C_S_L(
			groupId, JournalArticleConstants.CLASSNAME_ID_DEFAULT,
			ddmStructureKey, locale, queryDefinition);
	}

	/**
	 * Returns the number of web content articles belonging to the group.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @return the number of web content articles belonging to the group
	 */
	@Override
	public int getArticlesCount(long groupId) {
		return journalArticlePersistence.countByGroupId(groupId);
	}

	/**
	 * Returns the number of web content articles matching the group and folder.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article's folder
	 * @return the number of matching web content articles
	 */
	@Override
	public int getArticlesCount(long groupId, long folderId) {
		return journalArticlePersistence.countByG_F(groupId, folderId);
	}

	/**
	 * Returns the number of web content articles matching the group, folder,
	 * and status.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article's folder
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @return the number of matching web content articles
	 */
	@Override
	public int getArticlesCount(long groupId, long folderId, int status) {
		return journalArticlePersistence.countByG_F_ST(
			groupId, folderId, status);
	}

	@Override
	public int getArticlesCount(long groupId, String articleId) {
		return journalArticlePersistence.countByG_A(groupId, articleId);
	}

	@Override
	public String getArticleTitle(long articlePK, Locale locale) {
		String languageId = LocaleUtil.toLanguageId(locale);

		return getArticleTitle(articlePK, languageId);
	}

	@Override
	public String getArticleTitle(long articlePK, String languageId) {
		JournalArticleLocalization journalArticleLocalization =
			journalArticleLocalizationPersistence.fetchByA_L(
				articlePK, languageId);

		if (journalArticleLocalization == null) {
			return null;
		}

		return journalArticleLocalization.getTitle();
	}

	@Override
	public Map<Locale, String> getArticleTitleMap(long articlePK) {
		Map<Locale, String> journalArticleLocalizationTitleMap =
			new HashMap<>();

		List<JournalArticleLocalization> journalArticleLocalizationList =
			journalArticleLocalizationPersistence.findByArticlePK(articlePK);

		for (JournalArticleLocalization journalArticleLocalization :
				journalArticleLocalizationList) {

			journalArticleLocalizationTitleMap.put(
				LocaleUtil.fromLanguageId(
					journalArticleLocalization.getLanguageId()),
				journalArticleLocalization.getTitle());
		}

		return journalArticleLocalizationTitleMap;
	}

	/**
	 * Returns an ordered range of all the web content articles matching the
	 * company, version, and workflow status.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @param  version the web content article's version
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @return the range of matching web content articles ordered by article ID
	 */
	@Override
	public List<JournalArticle> getCompanyArticles(
		long companyId, double version, int status, int start, int end) {

		if (status == WorkflowConstants.STATUS_ANY) {
			return journalArticlePersistence.findByC_V(
				companyId, version, start, end, new ArticleIDComparator(true));
		}

		return journalArticlePersistence.findByC_V_ST(
			companyId, version, status, start, end,
			new ArticleIDComparator(true));
	}

	/**
	 * Returns an ordered range of all the web content articles matching the
	 * company and workflow status.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @return the range of matching web content articles ordered by article ID
	 */
	@Override
	public List<JournalArticle> getCompanyArticles(
		long companyId, int status, int start, int end) {

		if (status == WorkflowConstants.STATUS_ANY) {
			return journalArticlePersistence.findByCompanyId(
				companyId, start, end, new ArticleIDComparator(true));
		}

		return journalArticlePersistence.findByC_ST(
			companyId, status, start, end, new ArticleIDComparator(true));
	}

	/**
	 * Returns the number of web content articles matching the company, version,
	 * and workflow status.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @param  version the web content article's version
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @return the number of matching web content articles
	 */
	@Override
	public int getCompanyArticlesCount(
		long companyId, double version, int status, int start, int end) {

		if (status == WorkflowConstants.STATUS_ANY) {
			return journalArticlePersistence.countByC_V(companyId, version);
		}

		return journalArticlePersistence.countByC_V_ST(
			companyId, version, status);
	}

	/**
	 * Returns the number of web content articles matching the company and
	 * workflow status.
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @return the number of matching web content articles
	 */
	@Override
	public int getCompanyArticlesCount(long companyId, int status) {
		if (status == WorkflowConstants.STATUS_ANY) {
			return journalArticlePersistence.countByCompanyId(companyId);
		}

		return journalArticlePersistence.countByC_ST(companyId, status);
	}

	/**
	 * Returns the matching web content article currently displayed or next to
	 * be displayed if no article is currently displayed.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @return the matching web content article currently displayed, or the next
	 *         one to be displayed if no version of the article is currently
	 *         displayed
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getDisplayArticle(long groupId, String articleId)
		throws PortalException {

		JournalArticle article = fetchDisplayArticle(groupId, articleId);

		if (article == null) {
			throw new NoSuchArticleException(
				StringBundler.concat(
					"No approved JournalArticle exists with the key {groupId=",
					groupId, ", articleId=", articleId, "}"));
		}

		return article;
	}

	/**
	 * Returns the web content article matching the URL title that is currently
	 * displayed or next to be displayed if no article is currently displayed.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  urlTitle the web content article's accessible URL title
	 * @return the web content article matching the URL title that is currently
	 *         displayed, or next one to be displayed if no version of the
	 *         article is currently displayed
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getDisplayArticleByUrlTitle(
			long groupId, String urlTitle)
		throws PortalException {

		List<JournalArticle> articles = null;

		FriendlyURLEntry friendlyURLEntry =
			friendlyURLEntryLocalService.fetchFriendlyURLEntry(
				groupId, JournalArticle.class, urlTitle);

		if (friendlyURLEntry != null) {
			articles = journalArticlePersistence.findByR_ST(
				friendlyURLEntry.getClassPK(),
				WorkflowConstants.STATUS_APPROVED, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, new ArticleVersionComparator());
		}
		else {
			articles = journalArticlePersistence.findByG_UT_ST(
				groupId,
				FriendlyURLNormalizerUtil.normalizeWithEncoding(urlTitle),
				WorkflowConstants.STATUS_APPROVED, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, new ArticleVersionComparator());
		}

		if (articles.isEmpty()) {
			throw new NoSuchArticleException(
				StringBundler.concat(
					"No JournalArticle exists with the key {groupId=", groupId,
					", urlTitle=", urlTitle, "}"));
		}

		Date now = new Date();

		for (JournalArticle article : articles) {
			Date displayDate = article.getDisplayDate();
			Date expirationDate = article.getExpirationDate();

			if ((displayDate != null) && displayDate.before(now) &&
				((expirationDate == null) || expirationDate.after(now))) {

				return article;
			}
		}

		return articles.get(0);
	}

	@Override
	public List<JournalArticle> getIndexableArticlesByDDMStructureKey(
		String[] ddmStructureKeys) {

		if (isReindexAllArticleVersions()) {
			return getStructureArticles(ddmStructureKeys);
		}

		List<JournalArticle> articles = new ArrayList<>();

		QueryDefinition<JournalArticle> approvedQueryDefinition =
			new QueryDefinition<>(
				WorkflowConstants.STATUS_APPROVED, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, new ArticleVersionComparator());

		articles.addAll(
			journalArticleFinder.findByG_C_S_L(
				0, JournalArticleConstants.CLASSNAME_ID_DEFAULT,
				ddmStructureKeys, LocaleUtil.getMostRelevantLocale(),
				approvedQueryDefinition));

		QueryDefinition<JournalArticle> trashQueryDefinition =
			new QueryDefinition<>(
				WorkflowConstants.STATUS_IN_TRASH, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, new ArticleVersionComparator());

		articles.addAll(
			journalArticleFinder.findByG_C_S_L(
				0, JournalArticleConstants.CLASSNAME_ID_DEFAULT,
				ddmStructureKeys, LocaleUtil.getMostRelevantLocale(),
				trashQueryDefinition));

		return articles;
	}

	@Override
	public List<JournalArticle> getIndexableArticlesByDDMStructureKey(
		String[] ddmStructureKeys, Locale locale) {

		if (isReindexAllArticleVersions()) {
			return getStructureArticles(ddmStructureKeys);
		}

		QueryDefinition<JournalArticle> approvedQueryDefinition =
			new QueryDefinition<>(
				WorkflowConstants.STATUS_APPROVED, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, new ArticleVersionComparator());

		List<JournalArticle> articles = new ArrayList<>();

		articles.addAll(
			journalArticleFinder.findByG_C_S_L(
				0, JournalArticleConstants.CLASSNAME_ID_DEFAULT,
				ddmStructureKeys, locale, approvedQueryDefinition));

		QueryDefinition<JournalArticle> trashQueryDefinition =
			new QueryDefinition<>(
				WorkflowConstants.STATUS_IN_TRASH, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, new ArticleVersionComparator());

		articles.addAll(
			journalArticleFinder.findByG_C_S_L(
				0, JournalArticleConstants.CLASSNAME_ID_DEFAULT,
				ddmStructureKeys, locale, trashQueryDefinition));

		return articles;
	}

	/**
	 * Returns the indexable web content articles matching the resource primary
	 * key.
	 *
	 * @param  resourcePrimKey the primary key of the resource instance
	 * @return the indexable web content articles matching the resource primary
	 *         key
	 */
	@Override
	public List<JournalArticle> getIndexableArticlesByResourcePrimKey(
		long resourcePrimKey) {

		return journalArticlePersistence.findByR_I(resourcePrimKey, true);
	}

	/**
	 * Returns the latest web content article matching the resource primary key,
	 * preferring articles with approved workflow status.
	 *
	 * @param  resourcePrimKey the primary key of the resource instance
	 * @return the latest web content article matching the resource primary key,
	 *         preferring articles with approved workflow status
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getLatestArticle(long resourcePrimKey)
		throws PortalException {

		return getLatestArticle(resourcePrimKey, WorkflowConstants.STATUS_ANY);
	}

	/**
	 * Returns the latest web content article matching the resource primary key
	 * and workflow status, preferring articles with approved workflow status.
	 *
	 * @param  resourcePrimKey the primary key of the resource instance
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @return the latest web content article matching the resource primary key
	 *         and workflow status, preferring articles with approved workflow
	 *         status
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getLatestArticle(long resourcePrimKey, int status)
		throws PortalException {

		return getLatestArticle(resourcePrimKey, status, true);
	}

	/**
	 * Returns the latest web content article matching the resource primary key
	 * and workflow status, optionally preferring articles with approved
	 * workflow status.
	 *
	 * @param  resourcePrimKey the primary key of the resource instance
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  preferApproved whether to prefer returning the latest matching
	 *         article that has workflow status {@link
	 *         WorkflowConstants#STATUS_APPROVED} over returning one that has a
	 *         different status
	 * @return the latest web content article matching the resource primary key
	 *         and workflow status, optionally preferring articles with approved
	 *         workflow status
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getLatestArticle(
			long resourcePrimKey, int status, boolean preferApproved)
		throws PortalException {

		List<JournalArticle> articles = null;

		OrderByComparator<JournalArticle> orderByComparator =
			new ArticleVersionComparator();

		if (status == WorkflowConstants.STATUS_ANY) {
			if (preferApproved) {
				articles = journalArticlePersistence.findByR_ST(
					resourcePrimKey, WorkflowConstants.STATUS_APPROVED, 0, 1,
					orderByComparator);
			}

			if (ListUtil.isEmpty(articles)) {
				articles = journalArticlePersistence.findByResourcePrimKey(
					resourcePrimKey, 0, 1, orderByComparator);
			}
		}
		else {
			articles = journalArticlePersistence.findByR_ST(
				resourcePrimKey, status, 0, 1, orderByComparator);
		}

		if (articles.isEmpty()) {
			throw new NoSuchArticleException(
				"No JournalArticle exists with the key {resourcePrimKey=" +
					resourcePrimKey + "}");
		}

		return articles.get(0);
	}

	/**
	 * Returns the latest web content article with the group and article ID.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @return the latest matching web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getLatestArticle(long groupId, String articleId)
		throws PortalException {

		return getLatestArticle(
			groupId, articleId, WorkflowConstants.STATUS_ANY);
	}

	/**
	 * Returns the latest web content article matching the group, article ID,
	 * and workflow status.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @return the latest matching web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getLatestArticle(
			long groupId, String articleId, int status)
		throws PortalException {

		return getFirstArticle(
			groupId, articleId, status, new ArticleVersionComparator());
	}

	/**
	 * Returns the latest web content article matching the group, class name ID,
	 * and class PK.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  className the DDMStructure class name if the web content article
	 *         is related to a DDM structure, the class name associated with the
	 *         article, or JournalArticleConstants.CLASSNAME_ID_DEFAULT in the
	 *         journal-api module otherwise
	 * @param  classPK the primary key of the DDM structure, if the DDMStructure
	 *         class name is given as the <code>className</code> parameter, the
	 *         primary key of the class associated with the web content article,
	 *         or <code>0</code> otherwise
	 * @return the latest matching web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getLatestArticle(
			long groupId, String className, long classPK)
		throws PortalException {

		List<JournalArticle> articles = journalArticlePersistence.findByG_C_C(
			groupId, classNameLocalService.getClassNameId(className), classPK,
			0, 1, new ArticleVersionComparator());

		if (articles.isEmpty()) {
			throw new NoSuchArticleException(
				StringBundler.concat(
					"No JournalArticle exists with the key {groupId=", groupId,
					", className=", className, ", classPK =", classPK, "}"));
		}

		return articles.get(0);
	}

	/**
	 * Returns the latest web content article matching the group, URL title, and
	 * workflow status.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  urlTitle the web content article's accessible URL title
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @return the latest matching web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getLatestArticleByUrlTitle(
			long groupId, String urlTitle, int status)
		throws PortalException {

		JournalArticle article = null;

		FriendlyURLEntry friendlyURLEntry =
			friendlyURLEntryLocalService.fetchFriendlyURLEntry(
				groupId, JournalArticle.class, urlTitle);

		if (friendlyURLEntry != null) {
			article = fetchLatestArticle(friendlyURLEntry.getClassPK(), status);
		}
		else {
			article = fetchLatestArticleByUrlTitle(groupId, urlTitle, status);
		}

		if (article == null) {
			throw new NoSuchArticleException(
				StringBundler.concat(
					"No JournalArticle exists with the key {groupId=", groupId,
					", urlTitle=", urlTitle, ", status=", status, "}"));
		}

		return article;
	}

	/**
	 * Returns the latest version number of the web content with the group and
	 * article ID.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @return the latest version number of the matching web content
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public double getLatestVersion(long groupId, String articleId)
		throws PortalException {

		JournalArticle article = getLatestArticle(groupId, articleId);

		return article.getVersion();
	}

	/**
	 * Returns the latest version number of the web content with the group,
	 * article ID, and workflow status.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @return the latest version number of the matching web content
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public double getLatestVersion(long groupId, String articleId, int status)
		throws PortalException {

		JournalArticle article = getLatestArticle(groupId, articleId, status);

		return article.getVersion();
	}

	@Override
	public List<JournalArticle> getNoAssetArticles() {
		return journalArticleFinder.findByNoAssets();
	}

	@Override
	public List<JournalArticle> getNoPermissionArticles() {
		return journalArticleFinder.findByNoPermissions();
	}

	/**
	 * Returns the number of web content articles that are not recycled.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article folder
	 * @return the number of web content articles that are not recycled
	 */
	@Override
	public int getNotInTrashArticlesCount(long groupId, long folderId) {
		QueryDefinition<JournalArticle> queryDefinition = new QueryDefinition<>(
			WorkflowConstants.STATUS_ANY);

		return journalArticleFinder.countByG_F(
			groupId, ListUtil.toList(folderId), queryDefinition);
	}

	/**
	 * Returns the oldest web content article with the group and article ID.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @return the oldest matching web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getOldestArticle(long groupId, String articleId)
		throws PortalException {

		return getOldestArticle(
			groupId, articleId, WorkflowConstants.STATUS_ANY);
	}

	/**
	 * Returns the oldest web content article matching the group, article ID,
	 * and workflow status.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @return the oldest matching web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle getOldestArticle(
			long groupId, String articleId, int status)
		throws PortalException {

		return getFirstArticle(
			groupId, articleId, status, new ArticleVersionComparator(true));
	}

	/**
	 * Returns the previously approved version of the web content article. For
	 * more information on the approved workflow status, see {@link
	 * WorkflowConstants#STATUS_APPROVED}.
	 *
	 * @param  article the web content article
	 * @return the previously approved version of the web content article, or
	 *         the current web content article if there are no previously
	 *         approved web content articles
	 */
	@Override
	public JournalArticle getPreviousApprovedArticle(JournalArticle article) {
		List<JournalArticle> approvedArticles =
			journalArticlePersistence.findByG_A_ST(
				article.getGroupId(), article.getArticleId(),
				WorkflowConstants.STATUS_APPROVED, 0, 2);

		if (approvedArticles.isEmpty() ||
			((approvedArticles.size() == 1) &&
			 (article.getStatus() == WorkflowConstants.STATUS_APPROVED))) {

			return article;
		}

		JournalArticle previousApprovedArticle = approvedArticles.get(0);

		if (previousApprovedArticle.getVersion() == article.getVersion()) {
			previousApprovedArticle = approvedArticles.get(1);
		}

		return previousApprovedArticle;
	}

	/**
	 * Returns the web content articles matching the group and DDM structure
	 * key.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure
	 * @return the matching web content articles
	 */
	@Override
	public List<JournalArticle> getStructureArticles(
		long groupId, String ddmStructureKey) {

		return journalArticlePersistence.findByG_DDMSK(
			groupId, ddmStructureKey);
	}

	/**
	 * Returns an ordered range of all the web content articles matching the
	 * group and DDM structure key.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @param  obc the comparator to order the web content articles
	 * @return the range of matching web content articles ordered by the
	 *         comparator
	 */
	@Override
	public List<JournalArticle> getStructureArticles(
		long groupId, String ddmStructureKey, int start, int end,
		OrderByComparator<JournalArticle> obc) {

		return journalArticlePersistence.findByG_DDMSK(
			groupId, ddmStructureKey, start, end, obc);
	}

	/**
	 * Returns the web content articles matching the DDM structure keys.
	 *
	 * @param  ddmStructureKeys the primary keys of the web content article's
	 *         DDM structures
	 * @return the web content articles matching the DDM structure keys
	 */
	@Override
	public List<JournalArticle> getStructureArticles(
		String[] ddmStructureKeys) {

		return journalArticlePersistence.findByDDMStructureKey(
			ddmStructureKeys);
	}

	/**
	 * Returns the number of web content articles matching the group and DDM
	 * structure key.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure
	 * @return the number of matching web content articles
	 */
	@Override
	public int getStructureArticlesCount(long groupId, String ddmStructureKey) {
		return journalArticlePersistence.countByG_DDMSK(
			groupId, ddmStructureKey);
	}

	/**
	 * @deprecated As of Judson (7.1.x), with no direct replacement
	 */
	@Deprecated
	@Override
	public com.liferay.portal.kernel.service.SubscriptionLocalService
		getSubscriptionLocalService() {

		return null;
	}

	/**
	 * Returns the web content articles matching the group and DDM template key.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @return the matching web content articles
	 */
	@Override
	public List<JournalArticle> getTemplateArticles(
		long groupId, String ddmTemplateKey) {

		return journalArticlePersistence.findByG_DDMTK(groupId, ddmTemplateKey);
	}

	/**
	 * Returns an ordered range of all the web content articles matching the
	 * group and DDM template key.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @param  obc the comparator to order the web content articles
	 * @return the range of matching web content articles ordered by the
	 *         comparator
	 */
	@Override
	public List<JournalArticle> getTemplateArticles(
		long groupId, String ddmTemplateKey, int start, int end,
		OrderByComparator<JournalArticle> obc) {

		return journalArticlePersistence.findByG_DDMTK(
			groupId, ddmTemplateKey, start, end, obc);
	}

	/**
	 * Returns the number of web content articles matching the group and DDM
	 * template key.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @return the number of matching web content articles
	 */
	@Override
	public int getTemplateArticlesCount(long groupId, String ddmTemplateKey) {
		return journalArticlePersistence.countByG_DDMTK(
			groupId, ddmTemplateKey);
	}

	/**
	 * Returns the web content article's unique URL title.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  urlTitle the web content article's accessible URL title
	 * @return the web content article's unique URL title
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public String getUniqueUrlTitle(
			long groupId, String articleId, String urlTitle)
		throws PortalException {

		for (int i = 1;; i++) {
			JournalArticle article = fetchArticleByUrlTitle(groupId, urlTitle);

			if ((article == null) || articleId.equals(article.getArticleId())) {
				break;
			}

			String suffix = StringPool.DASH + i;

			String prefix = urlTitle;

			if (urlTitle.length() > suffix.length()) {
				prefix = urlTitle.substring(
					0, urlTitle.length() - suffix.length());
			}

			urlTitle = prefix + suffix;
		}

		return urlTitle;
	}

	/**
	 * Returns <code>true</code> if the specified web content article exists.
	 *
	 * @param  groupId the primary key of the group
	 * @param  articleId the primary key of the web content article
	 * @return <code>true</code> if the specified web content article exists;
	 *         <code>false</code> otherwise
	 */
	@Override
	public boolean hasArticle(long groupId, String articleId) {
		JournalArticle article = fetchArticle(groupId, articleId);

		if (article != null) {
			return true;
		}

		return false;
	}

	/**
	 * Returns <code>true</code> if the web content article, specified by group
	 * and article ID, is the latest version.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @return <code>true</code> if the specified web content article is the
	 *         latest version; <code>false</code> otherwise
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public boolean isLatestVersion(
			long groupId, String articleId, double version)
		throws PortalException {

		if (getLatestVersion(groupId, articleId) == version) {
			return true;
		}

		return false;
	}

	/**
	 * Returns <code>true</code> if the web content article, specified by group,
	 * article ID, and workflow status, is the latest version.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @return <code>true</code> if the specified web content article is the
	 *         latest version; <code>false</code> otherwise
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public boolean isLatestVersion(
			long groupId, String articleId, double version, int status)
		throws PortalException {

		if (getLatestVersion(groupId, articleId, status) == version) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isListable(JournalArticle article) {
		if ((article != null) && article.isIndexable()) {
			return true;
		}

		return false;
	}

	@Override
	public boolean isRenderable(
		JournalArticle article, PortletRequestModel portletRequestModel,
		ThemeDisplay themeDisplay) {

		try {
			getArticleDisplay(
				article, article.getDDMTemplateKey(), Constants.VIEW,
				article.getDefaultLanguageId(), 0, portletRequestModel,
				themeDisplay, true);
		}
		catch (Exception e) {
			return false;
		}

		return true;
	}

	/**
	 * Moves the web content article matching the group and article ID to a new
	 * folder.
	 *
	 * @param      groupId the primary key of the web content article's group
	 * @param      articleId the primary key of the web content article
	 * @param      newFolderId the primary key of the web content article's new
	 *             folder
	 * @return     the updated web content article, which was moved to a new
	 *             folder
	 * @throws     PortalException if a portal exception occurred
	 * @deprecated As of Wilberforce (7.0.x), replaced by {@link
	 *             #moveArticle(long, String, long, ServiceContext)}
	 */
	@Deprecated
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle moveArticle(
			long groupId, String articleId, long newFolderId)
		throws PortalException {

		return moveArticle(groupId, articleId, newFolderId, null);
	}

	/**
	 * Moves the web content article matching the group and article ID to a new
	 * folder.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  newFolderId the primary key of the web content article's new
	 *         folder
	 * @param  serviceContext the service context to be applied. Can set the
	 *         user ID, language ID, portlet preferences, portlet request,
	 *         portlet response, theme display, and can set whether to add the
	 *         default command update for the web content article. With respect
	 *         to social activities, by setting the service context's command to
	 *         {@link Constants#UPDATE}, the invocation is considered a web
	 *         content update activity; otherwise it is considered a web content
	 *         add activity.
	 * @return the updated web content article, which was moved to a new folder
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle moveArticle(
			long groupId, String articleId, long newFolderId,
			ServiceContext serviceContext)
		throws PortalException {

		JournalArticle latestArticle = getLatestArticle(groupId, articleId);

		validateDDMStructureId(
			groupId, newFolderId, latestArticle.getDDMStructureKey());

		List<JournalArticle> articles = journalArticlePersistence.findByG_A(
			groupId, articleId);

		String treePath = null;

		for (JournalArticle article : articles) {
			article.setFolderId(newFolderId);

			if (treePath == null) {
				treePath = article.buildTreePath();
			}

			article.setTreePath(treePath);

			journalArticlePersistence.update(article);
		}

		if (serviceContext != null) {
			notifySubscribers(
				serviceContext.getUserId(), latestArticle, "move_from",
				serviceContext);

			latestArticle.setFolderId(newFolderId);
			latestArticle.setTreePath(latestArticle.buildTreePath());

			notifySubscribers(
				serviceContext.getUserId(), latestArticle, "move_to",
				serviceContext);
		}

		return getArticle(groupId, articleId);
	}

	/**
	 * Moves the web content article from the Recycle Bin to a new folder.
	 *
	 * @param  userId the primary key of the user updating the web content
	 *         article
	 * @param  groupId the primary key of the web content article's group
	 * @param  article the web content article
	 * @param  newFolderId the primary key of the web content article's new
	 *         folder
	 * @param  serviceContext the service context to be applied. Can set the
	 *         modification date, portlet preferences, and can set whether to
	 *         add the default command update for the web content article. With
	 *         respect to social activities, by setting the service context's
	 *         command to {@link Constants#UPDATE}, the invocation is considered
	 *         a web content update activity; otherwise it is considered a web
	 *         content add activity.
	 * @return the updated web content article, which was moved from the Recycle
	 *         Bin to a new folder
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle moveArticleFromTrash(
			long userId, long groupId, JournalArticle article, long newFolderId,
			ServiceContext serviceContext)
		throws PortalException {

		if (!article.isInTrash()) {
			throw new RestoreEntryException(
				RestoreEntryException.INVALID_STATUS);
		}

		if (article.isInTrashExplicitly()) {
			article = restoreArticleFromTrash(userId, article);
		}
		else {

			// Article

			TrashVersion trashVersion = trashVersionLocalService.fetchVersion(
				JournalArticle.class.getName(), article.getResourcePrimKey());

			int status = WorkflowConstants.STATUS_APPROVED;

			if (trashVersion != null) {
				status = trashVersion.getStatus();
			}

			article = updateStatus(
				userId, article, status, null, serviceContext, new HashMap<>());

			// Attachments

			for (FileEntry fileEntry : article.getImagesFileEntries()) {
				PortletFileRepositoryUtil.restorePortletFileEntryFromTrash(
					userId, fileEntry.getFileEntryId());
			}

			// Trash

			if (trashVersion != null) {
				trashVersionLocalService.deleteTrashVersion(trashVersion);
			}
		}

		return moveArticle(
			groupId, article.getArticleId(), newFolderId, serviceContext);
	}

	/**
	 * Moves the latest version of the web content article matching the group
	 * and article ID to the recycle bin.
	 *
	 * @param  userId the primary key of the user updating the web content
	 *         article
	 * @param  article the web content article
	 * @return the updated web content article, which was moved to the Recycle
	 *         Bin
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle moveArticleToTrash(
			long userId, JournalArticle article)
		throws PortalException {

		// Article

		if (article.isInTrash()) {
			throw new TrashEntryException();
		}

		int oldStatus = article.getStatus();

		if (oldStatus == WorkflowConstants.STATUS_PENDING) {
			article.setStatus(WorkflowConstants.STATUS_DRAFT);

			journalArticlePersistence.update(article);
		}

		List<JournalArticle> articleVersions =
			journalArticlePersistence.findByG_A(
				article.getGroupId(), article.getArticleId());

		articleVersions = ListUtil.sort(
			articleVersions, new ArticleVersionComparator());

		List<ObjectValuePair<Long, Integer>> articleVersionStatusOVPs =
			new ArrayList<>();

		if ((articleVersions != null) && !articleVersions.isEmpty()) {
			articleVersionStatusOVPs = getArticleVersionStatuses(
				articleVersions);
		}

		article = updateStatus(
			userId, article.getId(), WorkflowConstants.STATUS_IN_TRASH,
			new HashMap<>(), new ServiceContext());

		// Trash

		JournalArticleResource articleResource =
			_journalArticleResourceLocalService.getArticleResource(
				article.getResourcePrimKey());

		UnicodeProperties typeSettingsProperties = new UnicodeProperties();

		typeSettingsProperties.put("title", article.getArticleId());

		TrashEntry trashEntry = trashEntryLocalService.addTrashEntry(
			userId, article.getGroupId(), JournalArticle.class.getName(),
			article.getResourcePrimKey(), articleResource.getUuid(), null,
			oldStatus, articleVersionStatusOVPs, typeSettingsProperties);

		String trashArticleId = _trashHelper.getTrashTitle(
			trashEntry.getEntryId());

		for (JournalArticle articleVersion : articleVersions) {
			articleVersion.setArticleId(trashArticleId);
			articleVersion.setStatus(WorkflowConstants.STATUS_IN_TRASH);

			journalArticlePersistence.update(articleVersion);
		}

		articleResource.setArticleId(trashArticleId);

		journalArticleResourcePersistence.update(articleResource);

		article.setArticleId(trashArticleId);

		article = journalArticlePersistence.update(article);

		// Asset

		assetEntryLocalService.updateVisible(
			JournalArticle.class.getName(), article.getResourcePrimKey(),
			false);

		// Attachments

		for (FileEntry fileEntry : article.getImagesFileEntries()) {
			PortletFileRepositoryUtil.movePortletFileEntryToTrash(
				userId, fileEntry.getFileEntryId());
		}

		// Comment

		if (isArticleCommentsEnabled(article.getCompanyId())) {
			_commentManager.moveDiscussionToTrash(
				JournalArticle.class.getName(), article.getResourcePrimKey());
		}

		// Social

		JSONObject extraDataJSONObject = JSONUtil.put(
			"title", article.getTitleMapAsXML());

		SocialActivityManagerUtil.addActivity(
			userId, article, SocialActivityConstants.TYPE_MOVE_TO_TRASH,
			extraDataJSONObject.toString(), 0);

		if (oldStatus == WorkflowConstants.STATUS_PENDING) {
			workflowInstanceLinkLocalService.deleteWorkflowInstanceLink(
				article.getCompanyId(), article.getGroupId(),
				JournalArticle.class.getName(), article.getId());
		}

		return article;
	}

	/**
	 * Moves the latest version of the web content article matching the group
	 * and article ID to the recycle bin.
	 *
	 * @param  userId the primary key of the user updating the web content
	 *         article
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @return the moved web content article or <code>null</code> if no matching
	 *         article was found
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle moveArticleToTrash(
			long userId, long groupId, String articleId)
		throws PortalException {

		List<JournalArticle> articles = journalArticlePersistence.findByG_A(
			groupId, articleId, 0, 1, new ArticleVersionComparator());

		if (!articles.isEmpty()) {
			return journalArticleLocalService.moveArticleToTrash(
				userId, articles.get(0));
		}

		return null;
	}

	/**
	 * Rebuilds the web content article's tree path using tree traversal.
	 *
	 * <p>
	 * For example, here is a conceptualization of a web content article tree
	 * path:
	 * </p>
	 *
	 * <p>
	 * <pre>
	 * <code>
	 * /(Folder's folderId)/(Subfolder's folderId)/(article's articleId)
	 * </code>
	 * </pre></p>
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void rebuildTree(long companyId) throws PortalException {
		JournalTreePathUtil.rebuildTree(
			companyId, JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID,
			StringPool.SLASH, journalFolderPersistence, this);
	}

	/**
	 * Removes the web content of the web content article matching the group,
	 * article ID, and version, and language.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @param  languageId the primary key of the language locale to remove
	 * @return the updated web content article with the locale removed
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle removeArticleLocale(
			long groupId, String articleId, double version, String languageId)
		throws PortalException {

		JournalArticle article = journalArticlePersistence.findByG_A_V(
			groupId, articleId, version);

		if (Objects.equals(languageId, article.getDefaultLanguageId())) {
			throw new RequiredArticleLocalizationException(
				"Default article localization is required");
		}

		journalArticleLocalizationPersistence.removeByA_L(
			article.getId(), languageId);

		Document document = article.getDocument();

		if (document != null) {
			String content = article.getContent();

			content = JournalUtil.removeArticleLocale(
				document, content, languageId);

			article.setContent(content);
		}

		journalArticlePersistence.update(article);

		return article;
	}

	/**
	 * Restores the web content article from the Recycle Bin.
	 *
	 * @param  userId the primary key of the user restoring the web content
	 *         article
	 * @param  article the web content article
	 * @return the restored web content article from the Recycle Bin
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle restoreArticleFromTrash(
			long userId, JournalArticle article)
		throws PortalException {

		// Article

		if (!article.isInTrash()) {
			throw new RestoreEntryException(
				RestoreEntryException.INVALID_STATUS);
		}

		String trashArticleId = _trashHelper.getOriginalTitle(
			article.getArticleId());

		List<JournalArticle> articleVersions =
			journalArticlePersistence.findByG_A(
				article.getGroupId(), article.getArticleId());

		for (JournalArticle articleVersion : articleVersions) {
			articleVersion.setArticleId(trashArticleId);

			articleVersion = journalArticlePersistence.update(articleVersion);

			if (article.equals(articleVersion)) {
				article = articleVersion;
			}
		}

		article.setArticleId(trashArticleId);

		article = journalArticlePersistence.update(article);

		JournalArticleResource articleResource =
			journalArticleResourcePersistence.fetchByPrimaryKey(
				article.getResourcePrimKey());

		articleResource.setArticleId(trashArticleId);

		journalArticleResourcePersistence.update(articleResource);

		TrashEntry trashEntry = trashEntryLocalService.getEntry(
			JournalArticle.class.getName(), article.getResourcePrimKey());

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setScopeGroupId(article.getGroupId());

		article = updateStatus(
			userId, article, trashEntry.getStatus(), null, serviceContext,
			new HashMap<>());

		// Trash

		List<TrashVersion> trashVersions = trashVersionLocalService.getVersions(
			trashEntry.getEntryId());

		boolean visible = false;

		for (TrashVersion trashVersion : trashVersions) {
			JournalArticle trashArticleVersion =
				journalArticlePersistence.findByPrimaryKey(
					trashVersion.getClassPK());

			trashArticleVersion.setStatus(trashVersion.getStatus());

			if (trashVersion.getStatus() == WorkflowConstants.STATUS_APPROVED) {
				visible = true;
			}

			journalArticlePersistence.update(trashArticleVersion);
		}

		trashEntryLocalService.deleteEntry(
			JournalArticle.class.getName(), article.getResourcePrimKey());

		if (visible) {
			assetEntryLocalService.updateVisible(
				JournalArticle.class.getName(), article.getResourcePrimKey(),
				true);
		}

		// Comment

		if (isArticleCommentsEnabled(article.getCompanyId())) {
			_commentManager.restoreDiscussionFromTrash(
				JournalArticle.class.getName(), article.getResourcePrimKey());
		}

		// Social

		JSONObject extraDataJSONObject = JSONUtil.put(
			"title", article.getTitleMapAsXML());

		SocialActivityManagerUtil.addActivity(
			userId, article, SocialActivityConstants.TYPE_RESTORE_FROM_TRASH,
			extraDataJSONObject.toString(), 0);

		return article;
	}

	/**
	 * Returns a range of all the web content articles matching the parameters
	 * without using the indexer. It is preferable to use the indexed version
	 * {@link #search(long, long, long, int, int, int)} instead of this method
	 * wherever possible for performance reasons.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param      groupId the primary key of the group (optionally
	 *             <code>0</code>)
	 * @param      folderIds the primary keys of the web content article folders
	 *             (optionally {@link Collections#EMPTY_LIST})
	 * @param      status the web content article's workflow status. For more
	 *             information see {@link WorkflowConstants} for constants
	 *             starting with the "STATUS_" prefix.
	 * @param      start the lower bound of the range of web content articles to
	 *             return
	 * @param      end the upper bound of the range of web content articles to
	 *             return (not inclusive)
	 * @return     the matching web content articles
	 * @deprecated As of Judson (7.1.x), replaced by {@link #search(long
	 *             groupId, List folderIds, Locale locale, int status, int
	 *             start, int end)}
	 */
	@Deprecated
	@Override
	public List<JournalArticle> search(
		long groupId, List<Long> folderIds, int status, int start, int end) {

		Locale locale = LocaleUtil.getMostRelevantLocale();

		return search(groupId, folderIds, locale, status, start, end);
	}

	@Override
	public List<JournalArticle> search(
		long groupId, List<Long> folderIds, Locale locale, int status,
		int start, int end) {

		QueryDefinition<JournalArticle> queryDefinition = new QueryDefinition<>(
			status, start, end, null);

		return journalArticleFinder.findByG_F_L(
			groupId, folderIds, locale, queryDefinition);
	}

	/**
	 * Returns a range of all the web content articles in a single folder
	 * matching the parameters without using the indexer. It is preferable to
	 * use the indexed version {@link #search(long, long, long, int, int, int)}
	 * instead of this method wherever possible for performance reasons.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  folderId the primary key of the web content article folder
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @return the matching web content articles
	 */
	@Override
	public List<JournalArticle> search(
		long groupId, long folderId, int status, int start, int end) {

		return search(
			groupId, ListUtil.toList(folderId),
			LocaleUtil.getMostRelevantLocale(), status, start, end);
	}

	/**
	 * Returns an ordered range of all the web content articles matching the
	 * parameters without using the indexer, including a keywords parameter for
	 * matching with the article's ID, title, description, and content, a DDM
	 * structure key parameter, and a DDM template key parameter. It is
	 * preferable to use the indexed version {@link #search(long, long, List,
	 * long, String, String, String, LinkedHashMap, int, int, Sort)} instead of
	 * this method wherever possible for performance reasons.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  folderIds the primary keys of the web content article folders
	 *         (optionally {@link Collections#EMPTY_LIST})
	 * @param  classNameId the primary key of the DDMStructure class if the web
	 *         content article is related to a DDM structure, the primary key of
	 *         the class name associated with the article, or
	 *         JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *         module otherwise
	 * @param  keywords the keywords (space separated), which may occur in the
	 *         web content article ID, title, description, or content
	 *         (optionally <code>null</code>). If the keywords value is not
	 *         <code>null</code>, the search uses the OR operator in connecting
	 *         query criteria; otherwise it uses the AND operator.
	 * @param  version the web content article's version (optionally
	 *         <code>null</code>)
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure, if the article is related to a DDM structure, or
	 *         <code>null</code> otherwise
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  displayDateGT the date after which a matching web content
	 *         article's display date must be after (optionally
	 *         <code>null</code>)
	 * @param  displayDateLT the date before which a matching web content
	 *         article's display date must be before (optionally
	 *         <code>null</code>)
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  reviewDate the web content article's scheduled review date
	 *         (optionally <code>null</code>)
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @param  obc the comparator to order the web content articles
	 * @return the range of matching web content articles ordered by the
	 *         comparator
	 */
	@Override
	public List<JournalArticle> search(
		long companyId, long groupId, List<Long> folderIds, long classNameId,
		String keywords, Double version, String ddmStructureKey,
		String ddmTemplateKey, Date displayDateGT, Date displayDateLT,
		int status, Date reviewDate, int start, int end,
		OrderByComparator<JournalArticle> obc) {

		return journalArticleFinder.findByKeywords(
			companyId, groupId, folderIds, classNameId, keywords, version,
			ddmStructureKey, ddmTemplateKey, displayDateGT, displayDateLT,
			status, reviewDate, start, end, obc);
	}

	/**
	 * Returns an ordered range of all the web content articles matching the
	 * parameters without using the indexer, including keyword parameters for
	 * article ID, title, description, and content, a DDM structure key
	 * parameter, a DDM template key parameter, and an AND operator switch. It
	 * is preferable to use the indexed version {@link #search(long, long, List,
	 * long, String, String, String, String, int, String, String, LinkedHashMap,
	 * boolean, int, int, Sort)} instead of this method wherever possible for
	 * performance reasons.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  folderIds the primary keys of the web content article folders
	 *         (optionally {@link Collections#EMPTY_LIST})
	 * @param  classNameId the primary key of the DDMStructure class if the web
	 *         content article is related to a DDM structure, the primary key of
	 *         the class name associated with the article, or
	 *         JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *         module otherwise
	 * @param  articleId the article ID keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  version the web content article's version (optionally
	 *         <code>null</code>)
	 * @param  title the title keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  description the description keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  content the content keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure, if the article is related to a DDM structure, or
	 *         <code>null</code> otherwise
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  displayDateGT the date after which a matching web content
	 *         article's display date must be after (optionally
	 *         <code>null</code>)
	 * @param  displayDateLT the date before which a matching web content
	 *         article's display date must be before (optionally
	 *         <code>null</code>)
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  reviewDate the web content article's scheduled review date
	 *         (optionally <code>null</code>)
	 * @param  andOperator whether every field must match its value or keywords,
	 *         or just one field must match. Company, group, folder IDs, class
	 *         name ID, and status must all match their values.
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @param  obc the comparator to order the web content articles
	 * @return the range of matching web content articles ordered by the
	 *         comparator
	 */
	@Override
	public List<JournalArticle> search(
		long companyId, long groupId, List<Long> folderIds, long classNameId,
		String articleId, Double version, String title, String description,
		String content, String ddmStructureKey, String ddmTemplateKey,
		Date displayDateGT, Date displayDateLT, int status, Date reviewDate,
		boolean andOperator, int start, int end,
		OrderByComparator<JournalArticle> obc) {

		QueryDefinition<JournalArticle> queryDefinition = new QueryDefinition<>(
			status, start, end, obc);

		return journalArticleFinder.findByC_G_F_C_A_V_T_D_C_S_T_D_R(
			companyId, groupId, folderIds, classNameId, articleId, version,
			title, description, content, ddmStructureKey, ddmTemplateKey,
			displayDateGT, displayDateLT, reviewDate, andOperator,
			queryDefinition);
	}

	/**
	 * Returns an ordered range of all the web content articles matching the
	 * parameters without using the indexer, including keyword parameters for
	 * article ID, title, description, and content, a DDM structure keys
	 * (plural) parameter, a DDM template keys (plural) parameter, and an AND
	 * operator switch.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  folderIds the primary keys of the web content article folders
	 *         (optionally {@link Collections#EMPTY_LIST})
	 * @param  classNameId the primary key of the DDMStructure class if the web
	 *         content article is related to a DDM structure, the primary key of
	 *         the class name associated with the article, or
	 *         JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *         module otherwise
	 * @param  articleId the article ID keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  version the web content article's version (optionally
	 *         <code>null</code>)
	 * @param  title the title keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  description the description keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  content the content keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  ddmStructureKeys the primary keys of the web content article's
	 *         DDM structures, if the article is related to a DDM structure, or
	 *         <code>null</code> otherwise
	 * @param  ddmTemplateKeys the primary keys of the web content article's DDM
	 *         templates (originally <code>null</code>). If the articles are
	 *         related to a DDM structure, the template's structure must match
	 *         it.
	 * @param  displayDateGT the date after which a matching web content
	 *         article's display date must be after (optionally
	 *         <code>null</code>)
	 * @param  displayDateLT the date before which a matching web content
	 *         article's display date must be before (optionally
	 *         <code>null</code>)
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  reviewDate the web content article's scheduled review date
	 *         (optionally <code>null</code>)
	 * @param  andOperator whether every field must match its value or keywords,
	 *         or just one field must match.  Company, group, folder IDs, class
	 *         name ID, and status must all match their values.
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @param  obc the comparator to order the web content articles
	 * @return the range of matching web content articles ordered by the
	 *         comparator
	 */
	@Override
	public List<JournalArticle> search(
		long companyId, long groupId, List<Long> folderIds, long classNameId,
		String articleId, Double version, String title, String description,
		String content, String[] ddmStructureKeys, String[] ddmTemplateKeys,
		Date displayDateGT, Date displayDateLT, int status, Date reviewDate,
		boolean andOperator, int start, int end,
		OrderByComparator<JournalArticle> obc) {

		QueryDefinition<JournalArticle> queryDefinition = new QueryDefinition<>(
			status, start, end, obc);

		return journalArticleFinder.findByC_G_F_C_A_V_T_D_C_S_T_D_R(
			companyId, groupId, folderIds, classNameId, articleId, version,
			title, description, content, ddmStructureKeys, ddmTemplateKeys,
			displayDateGT, displayDateLT, reviewDate, andOperator,
			queryDefinition);
	}

	/**
	 * Returns an ordered range of all the web content articles matching the
	 * parameters using the indexer, including a keywords parameter for matching
	 * an article's ID, title, description, or content, a DDM structure key
	 * parameter, a DDM template key parameter, and a finder hash map parameter.
	 * It is preferable to use this method instead of the non-indexed version
	 * whenever possible for performance reasons.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  folderIds the primary keys of the web content article folders
	 *         (optionally {@link Collections#EMPTY_LIST})
	 * @param  classNameId the primary key of the DDMStructure class if the web
	 *         content article is related to a DDM structure, the primary key of
	 *         the class name associated with the article, or
	 *         JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *         module otherwise
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure, if the article is related to a DDM structure, or
	 *         <code>null</code> otherwise
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  keywords the keywords (space separated), which may occur in the
	 *         web content article ID, title, description, or content
	 *         (optionally <code>null</code>). If the keywords value is not
	 *         <code>null</code>, the search uses the OR operator in connecting
	 *         query criteria; otherwise it uses the AND operator.
	 * @param  params the finder parameters (optionally <code>null</code>)
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @param  sort the field, type, and direction by which to sort (optionally
	 *         <code>null</code>)
	 * @return the matching web content articles ordered by <code>sort</code>
	 */
	@Override
	public Hits search(
		long companyId, long groupId, List<Long> folderIds, long classNameId,
		String ddmStructureKey, String ddmTemplateKey, String keywords,
		LinkedHashMap<String, Object> params, int start, int end, Sort sort) {

		String articleId = null;
		String title = null;
		String description = null;
		String content = null;
		boolean andOperator = false;

		if (Validator.isNotNull(keywords)) {
			articleId = keywords;
			title = keywords;
			description = keywords;
			content = keywords;
		}
		else {
			andOperator = true;
		}

		if (params != null) {
			params.put("keywords", keywords);
		}

		return search(
			companyId, groupId, folderIds, classNameId, articleId, title,
			description, content, WorkflowConstants.STATUS_ANY, ddmStructureKey,
			ddmTemplateKey, params, andOperator, start, end, sort);
	}

	/**
	 * Returns an ordered range of all the web content articles matching the
	 * parameters using the indexer, including a keywords parameter for matching
	 * an article's ID, title, description, or content, a DDM structure key
	 * parameter, a DDM template key parameter, an AND operator switch, and
	 * parameters for type, status, a finder hash map. It is preferable to use
	 * this method instead of the non-indexed version whenever possible for
	 * performance reasons.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  folderIds the primary keys of the web content article folders
	 *         (optionally {@link Collections#EMPTY_LIST})
	 * @param  classNameId the primary key of the DDMStructure class if the web
	 *         content article is related to a DDM structure, the primary key of
	 *         the class name associated with the article, or
	 *         JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *         module otherwise
	 * @param  articleId the article ID keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  title the title keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  description the description keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  content the content keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure, if the article is related to a DDM structure, or
	 *         <code>null</code> otherwise
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  params the finder parameters (optionally <code>null</code>). The
	 *         <code>includeDiscussions</code> parameter can be set to
	 *         <code>true</code> to search for the keywords in the web content
	 *         article discussions.
	 * @param  andSearch whether every field must match its value or keywords,
	 *         or just one field must match
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @param  sort the field, type, and direction by which to sort (optionally
	 *         <code>null</code>)
	 * @return the matching web content articles ordered by <code>sort</code>
	 */
	@Override
	public Hits search(
		long companyId, long groupId, List<Long> folderIds, long classNameId,
		String articleId, String title, String description, String content,
		int status, String ddmStructureKey, String ddmTemplateKey,
		LinkedHashMap<String, Object> params, boolean andSearch, int start,
		int end, Sort sort) {

		try {
			Indexer<JournalArticle> indexer =
				IndexerRegistryUtil.nullSafeGetIndexer(JournalArticle.class);

			SearchContext searchContext = buildSearchContext(
				companyId, groupId, folderIds, classNameId, articleId, title,
				description, content, status, ddmStructureKey, ddmTemplateKey,
				params, andSearch, start, end, sort);

			return indexer.search(searchContext);
		}
		catch (Exception e) {
			throw new SystemException(e);
		}
	}

	/**
	 * @deprecated As of Wilberforce (7.0.x), replaced by {@link #search(long,
	 *             long, List, long, String, String, String, String, int,
	 *             String, String, LinkedHashMap, boolean, int, int, Sort)}
	 */
	@Deprecated
	@Override
	public Hits search(
		long companyId, long groupId, List<Long> folderIds, long classNameId,
		String articleId, String title, String description, String content,
		String type, String statusString, String ddmStructureKey,
		String ddmTemplateKey, LinkedHashMap<String, Object> params,
		boolean andSearch, int start, int end, Sort sort) {

		int status = GetterUtil.getInteger(statusString);

		return search(
			companyId, groupId, folderIds, classNameId, articleId, title,
			description, content, status, ddmStructureKey, ddmTemplateKey,
			params, andSearch, start, end, sort);
	}

	/**
	 * Returns a range of all the web content articles matching the group,
	 * creator, and status using the indexer. It is preferable to use this
	 * method instead of the non-indexed version whenever possible for
	 * performance reasons.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  userId the primary key of the user searching for web content
	 *         articles
	 * @param  creatorUserId the primary key of the web content article's
	 *         creator
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @return the matching web content articles
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public Hits search(
			long groupId, long userId, long creatorUserId, int status,
			int start, int end)
		throws PortalException {

		Indexer<JournalArticle> indexer = IndexerRegistryUtil.getIndexer(
			JournalArticle.class.getName());

		SearchContext searchContext = buildSearchContext(
			groupId, userId, creatorUserId, status, start, end);

		return indexer.search(searchContext);
	}

	/**
	 * Returns the number of web content articles matching the group, folders,
	 * and status.
	 *
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  folderIds the primary keys of the web content article folders
	 *         (optionally {@link Collections#EMPTY_LIST})
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @return the number of matching web content articles
	 */
	@Override
	public int searchCount(long groupId, List<Long> folderIds, int status) {
		QueryDefinition<JournalArticle> queryDefinition = new QueryDefinition<>(
			status);

		return journalArticleFinder.countByG_F(
			groupId, folderIds, queryDefinition);
	}

	/**
	 * Returns the number of web content articles matching the group, folder,
	 * and status.
	 *
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  folderId the primary key of the web content article folder
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @return the number of matching web content articles
	 */
	@Override
	public int searchCount(long groupId, long folderId, int status) {
		return searchCount(groupId, ListUtil.toList(folderId), status);
	}

	/**
	 * Returns the number of web content articles matching the parameters,
	 * including a keywords parameter for matching with the article's ID, title,
	 * description, and content, a DDM structure key parameter, and a DDM
	 * template key parameter.
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  folderIds the primary keys of the web content article folders
	 *         (optionally {@link Collections#EMPTY_LIST})
	 * @param  classNameId the primary key of the DDMStructure class if the web
	 *         content article is related to a DDM structure, the primary key of
	 *         the class name associated with the article, or
	 *         JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *         module otherwise
	 * @param  keywords the keywords (space separated), which may occur in the
	 *         web content article ID, title, description, or content
	 *         (optionally <code>null</code>). If the keywords value is not
	 *         <code>null</code>, the search uses the OR operator in connecting
	 *         query criteria; otherwise it uses the AND operator.
	 * @param  version the web content article's version (optionally
	 *         <code>null</code>)
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure, if the article is related to a DDM structure, or
	 *         <code>null</code> otherwise
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  displayDateGT the date after which a matching web content
	 *         article's display date must be after (optionally
	 *         <code>null</code>)
	 * @param  displayDateLT the date before which a matching web content
	 *         article's display date must be before (optionally
	 *         <code>null</code>)
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  reviewDate the web content article's scheduled review date
	 *         (optionally <code>null</code>)
	 * @return the number of matching web content articles
	 */
	@Override
	public int searchCount(
		long companyId, long groupId, List<Long> folderIds, long classNameId,
		String keywords, Double version, String ddmStructureKey,
		String ddmTemplateKey, Date displayDateGT, Date displayDateLT,
		int status, Date reviewDate) {

		return journalArticleFinder.countByKeywords(
			companyId, groupId, folderIds, classNameId, keywords, version,
			ddmStructureKey, ddmTemplateKey, displayDateGT, displayDateLT,
			status, reviewDate);
	}

	/**
	 * Returns the number of web content articles matching the parameters,
	 * including keyword parameters for article ID, title, description, and
	 * content, a DDM structure key parameter, a DDM template key parameter, and
	 * an AND operator switch.
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  folderIds the primary keys of the web content article folders
	 *         (optionally {@link Collections#EMPTY_LIST})
	 * @param  classNameId the primary key of the DDMStructure class if the web
	 *         content article is related to a DDM structure, the primary key of
	 *         the class name associated with the article, or
	 *         JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *         module otherwise
	 * @param  articleId the article ID keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  version the web content article's version (optionally
	 *         <code>null</code>)
	 * @param  title the title keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  description the description keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  content the content keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure, if the article is related to a DDM structure, or
	 *         <code>null</code> otherwise
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  displayDateGT the date after which a matching web content
	 *         article's display date must be after (optionally
	 *         <code>null</code>)
	 * @param  displayDateLT the date before which a matching web content
	 *         article's display date must be before (optionally
	 *         <code>null</code>)
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  reviewDate the web content article's scheduled review date
	 *         (optionally <code>null</code>)
	 * @param  andOperator whether every field must match its value or keywords,
	 *         or just one field must match. Group, folder IDs, class name ID,
	 *         and status must all match their values.
	 * @return the number of matching web content articles
	 */
	@Override
	public int searchCount(
		long companyId, long groupId, List<Long> folderIds, long classNameId,
		String articleId, Double version, String title, String description,
		String content, String ddmStructureKey, String ddmTemplateKey,
		Date displayDateGT, Date displayDateLT, int status, Date reviewDate,
		boolean andOperator) {

		return journalArticleFinder.countByC_G_F_C_A_V_T_D_C_S_T_D_R(
			companyId, groupId, folderIds, classNameId, articleId, version,
			title, description, content, ddmStructureKey, ddmTemplateKey,
			displayDateGT, displayDateLT, reviewDate, andOperator,
			new QueryDefinition<JournalArticle>(status));
	}

	/**
	 * Returns the number of web content articles matching the parameters,
	 * including keyword parameters for article ID, title, description, and
	 * content, a DDM structure keys (plural) parameter, a DDM template keys
	 * (plural) parameter, and an AND operator switch.
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  folderIds the primary keys of the web content article folders
	 *         (optionally {@link Collections#EMPTY_LIST})
	 * @param  classNameId the primary key of the DDMStructure class if the web
	 *         content article is related to a DDM structure, the primary key of
	 *         the class name associated with the article, or
	 *         JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *         module otherwise
	 * @param  articleId the article ID keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  version the web content article's version (optionally
	 *         <code>null</code>)
	 * @param  title the title keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  description the description keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  content the content keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  ddmStructureKeys the primary keys of the web content article's
	 *         DDM structures, if the article is related to a DDM structure, or
	 *         <code>null</code> otherwise
	 * @param  ddmTemplateKeys the primary keys of the web content article's DDM
	 *         templates (originally <code>null</code>). If the articles are
	 *         related to a DDM structure, the template's structure must match
	 *         it.
	 * @param  displayDateGT the date after which a matching web content
	 *         article's display date must be after (optionally
	 *         <code>null</code>)
	 * @param  displayDateLT the date before which a matching web content
	 *         article's display date must be before (optionally
	 *         <code>null</code>)
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  reviewDate the web content article's scheduled review date
	 *         (optionally <code>null</code>)
	 * @param  andOperator whether every field must match its value or keywords,
	 *         or just one field must match.  Group, folder IDs, class name ID,
	 *         and status must all match their values.
	 * @return the number of matching web content articles
	 */
	@Override
	public int searchCount(
		long companyId, long groupId, List<Long> folderIds, long classNameId,
		String articleId, Double version, String title, String description,
		String content, String[] ddmStructureKeys, String[] ddmTemplateKeys,
		Date displayDateGT, Date displayDateLT, int status, Date reviewDate,
		boolean andOperator) {

		return journalArticleFinder.countByC_G_F_C_A_V_T_D_C_S_T_D_R(
			companyId, groupId, folderIds, classNameId, articleId, version,
			title, description, content, ddmStructureKeys, ddmTemplateKeys,
			displayDateGT, displayDateLT, reviewDate, andOperator,
			new QueryDefinition<JournalArticle>(status));
	}

	/**
	 * Returns a {@link BaseModelSearchResult} containing the total number of
	 * hits and an ordered range of all the web content articles matching the
	 * parameters using the indexer, including a keywords parameter for matching
	 * an article's ID, title, description, or content, a DDM structure key
	 * parameter, a DDM template key parameter, and a finder hash map parameter.
	 * It is preferable to use this method instead of the non-indexed version
	 * whenever possible for performance reasons.
	 *
	 * <p>
	 * The <code>start</code> and <code>end</code> parameters only affect the
	 * amount of web content articles returned as results, not the total number
	 * of hits.
	 * </p>
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  folderIds the primary keys of the web content article folders
	 *         (optionally {@link Collections#EMPTY_LIST})
	 * @param  classNameId the primary key of the DDMStructure class, the
	 *         primary key of the class name associated with the article, or
	 *         JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *         module otherwise
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  keywords the keywords (space separated), which may occur in the
	 *         web content article ID, title, description, or content
	 *         (optionally <code>null</code>). If the keywords value is not
	 *         <code>null</code>, the search uses the OR operator in connecting
	 *         query criteria; otherwise it uses the AND operator.
	 * @param  params the finder parameters (optionally <code>null</code>)
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @param  sort the field, type, and direction by which to sort (optionally
	 *         <code>null</code>)
	 * @return a {@link BaseModelSearchResult} containing the total number of
	 *         hits and an ordered range of all the matching web content
	 *         articles ordered by <code>sort</code>
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public BaseModelSearchResult<JournalArticle> searchJournalArticles(
			long companyId, long groupId, List<Long> folderIds,
			long classNameId, String ddmStructureKey, String ddmTemplateKey,
			String keywords, LinkedHashMap<String, Object> params, int start,
			int end, Sort sort)
		throws PortalException {

		String articleId = null;
		String title = null;
		String description = null;
		String content = null;
		boolean andOperator = false;

		if (Validator.isNotNull(keywords)) {
			articleId = keywords;
			title = keywords;
			description = keywords;
			content = keywords;
		}
		else {
			andOperator = true;
		}

		if (params != null) {
			params.put("keywords", keywords);
		}

		return searchJournalArticles(
			companyId, groupId, folderIds, classNameId, articleId, title,
			description, content, WorkflowConstants.STATUS_ANY, ddmStructureKey,
			ddmTemplateKey, params, andOperator, start, end, sort);
	}

	/**
	 * Returns a {@link BaseModelSearchResult} containing the total number of
	 * hits and an ordered range of all the web content articles matching the
	 * parameters using the indexer, including keyword parameters for article
	 * ID, title, description, or content, a DDM structure key parameter, a DDM
	 * template key parameter, an AND operator switch, and parameters for type,
	 * status, and a finder hash map. It is preferable to use this method
	 * instead of the non-indexed version whenever possible for performance
	 * reasons.
	 *
	 * <p>
	 * The <code>start</code> and <code>end</code> parameters only affect the
	 * amount of web content articles returned as results, not the total number
	 * of hits.
	 * </p>
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  companyId the primary key of the web content article's company
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  folderIds the primary keys of the web content article folders
	 *         (optionally {@link Collections#EMPTY_LIST})
	 * @param  classNameId the primary key of the DDMStructure class, the
	 *         primary key of the class name associated with the article, or
	 *         JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *         module otherwise
	 * @param  articleId the article ID keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  title the title keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  description the description keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  content the content keywords (space separated, optionally
	 *         <code>null</code>)
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  params the finder parameters (optionally <code>null</code>). The
	 *         <code>includeDiscussions</code> parameter can be set to
	 *         <code>true</code> to search for the keywords in the web content
	 *         article discussions.
	 * @param  andSearch whether every field must match its value or keywords,
	 *         or just one field must match
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @param  sort the field, type, and direction by which to sort (optionally
	 *         <code>null</code>)
	 * @return a {@link BaseModelSearchResult} containing the total number of
	 *         hits and an ordered range of all the matching web content
	 *         articles ordered by <code>sort</code>
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public BaseModelSearchResult<JournalArticle> searchJournalArticles(
			long companyId, long groupId, List<Long> folderIds,
			long classNameId, String articleId, String title,
			String description, String content, int status,
			String ddmStructureKey, String ddmTemplateKey,
			LinkedHashMap<String, Object> params, boolean andSearch, int start,
			int end, Sort sort)
		throws PortalException {

		SearchContext searchContext = buildSearchContext(
			companyId, groupId, folderIds, classNameId, articleId, title,
			description, content, status, ddmStructureKey, ddmTemplateKey,
			params, andSearch, start, end, sort);

		return searchJournalArticles(searchContext);
	}

	/**
	 * Returns a {@link BaseModelSearchResult} containing the total number of
	 * hits and an ordered range of all the web content articles matching the
	 * parameters using the indexer, including the web content article's creator
	 * ID and status. It is preferable to use this method instead of the
	 * non-indexed version whenever possible for performance reasons.
	 *
	 * <p>
	 * The <code>start</code> and <code>end</code> parameters only affect the
	 * amount of web content articles returned as results, not the total number
	 * of hits.
	 * </p>
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end -
	 * start</code> instances. <code>start</code> and <code>end</code> are not
	 * primary keys, they are indexes in the result set. Thus, <code>0</code>
	 * refers to the first result in the set. Setting both <code>start</code>
	 * and <code>end</code> to {@link QueryUtil#ALL_POS} will return the full
	 * result set.
	 * </p>
	 *
	 * @param  groupId the primary key of the group (optionally <code>0</code>)
	 * @param  userId the primary key of the user searching for web content
	 *         articles
	 * @param  creatorUserId the primary key of the web content article's
	 *         creator
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  start the lower bound of the range of web content articles to
	 *         return
	 * @param  end the upper bound of the range of web content articles to
	 *         return (not inclusive)
	 * @return a {@link BaseModelSearchResult} containing the total number of
	 *         hits and an ordered range of all the matching web content
	 *         articles ordered by <code>sort</code>
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public BaseModelSearchResult<JournalArticle> searchJournalArticles(
			long groupId, long userId, long creatorUserId, int status,
			int start, int end)
		throws PortalException {

		SearchContext searchContext = buildSearchContext(
			groupId, userId, creatorUserId, status, start, end);

		return searchJournalArticles(searchContext);
	}

	/**
	 * @deprecated As of Judson (7.1.x), with no direct replacement
	 */
	@Deprecated
	@Override
	public void setSubscriptionLocalService(
		com.liferay.portal.kernel.service.SubscriptionLocalService
			subscriptionLocalService) {
	}

	@Override
	public void setTreePaths(
			final long folderId, final String treePath, final boolean reindex)
		throws PortalException {

		if (treePath == null) {
			throw new IllegalArgumentException("Tree path is null");
		}

		final IndexableActionableDynamicQuery indexableActionableDynamicQuery =
			getIndexableActionableDynamicQuery();

		indexableActionableDynamicQuery.setAddCriteriaMethod(
			dynamicQuery -> {
				Property folderIdProperty = PropertyFactoryUtil.forName(
					"folderId");

				dynamicQuery.add(folderIdProperty.eq(folderId));

				Property treePathProperty = PropertyFactoryUtil.forName(
					"treePath");

				dynamicQuery.add(
					RestrictionsFactoryUtil.or(
						treePathProperty.isNull(),
						treePathProperty.ne(treePath)));
			});

		final Indexer<JournalArticle> indexer = IndexerRegistryUtil.getIndexer(
			JournalArticle.class.getName());

		indexableActionableDynamicQuery.setPerformActionMethod(
			(JournalArticle article) -> {
				article.setTreePath(treePath);

				updateJournalArticle(article);

				if (!reindex) {
					return;
				}

				indexableActionableDynamicQuery.setCompanyId(
					article.getCompanyId());

				indexableActionableDynamicQuery.addDocuments(
					indexer.getDocument(article));
			});

		indexableActionableDynamicQuery.performActions();
	}

	/**
	 * Subscribes the user to changes in elements that belong to the web content
	 * article.
	 *
	 * @param  userId the primary key of the user to be subscribed
	 * @param  groupId the primary key of the folder's group
	 * @param  articleId the primary key of the article to subscribe to
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void subscribe(long userId, long groupId, long articleId)
		throws PortalException {

		_subscriptionLocalService.addSubscription(
			userId, groupId, JournalArticle.class.getName(), articleId);
	}

	/**
	 * Subscribes the user to changes in elements that belong to the web content
	 * article's DDM structure.
	 *
	 * @param  groupId the primary key of the folder's group
	 * @param  userId the primary key of the user to be subscribed
	 * @param  ddmStructureId the primary key of the structure to subscribe to
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void subscribeStructure(
			long groupId, long userId, long ddmStructureId)
		throws PortalException {

		_subscriptionLocalService.addSubscription(
			userId, groupId, DDMStructure.class.getName(), ddmStructureId);
	}

	/**
	 * Unsubscribes the user from changes in elements that belong to the web
	 * content article.
	 *
	 * @param  userId the primary key of the user to be subscribed
	 * @param  groupId the primary key of the folder's group
	 * @param  articleId the primary key of the article to unsubscribe from
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void unsubscribe(long userId, long groupId, long articleId)
		throws PortalException {

		_subscriptionLocalService.deleteSubscription(
			userId, JournalArticle.class.getName(), articleId);
	}

	/**
	 * Unsubscribes the user from changes in elements that belong to the web
	 * content article's DDM structure.
	 *
	 * @param  groupId the primary key of the folder's group
	 * @param  userId the primary key of the user to be subscribed
	 * @param  ddmStructureId the primary key of the structure to subscribe to
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void unsubscribeStructure(
			long groupId, long userId, long ddmStructureId)
		throws PortalException {

		_subscriptionLocalService.deleteSubscription(
			userId, DDMStructure.class.getName(), ddmStructureId);
	}

	/**
	 * Updates the web content article with additional parameters. All
	 * scheduling parameters (display date, expiration date, and review date)
	 * use the current user's timezone.
	 *
	 * @param  userId the primary key of the user updating the web content
	 *         article
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article folder
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @param  titleMap the web content article's locales and localized titles
	 * @param  descriptionMap the web content article's locales and localized
	 *         descriptions
	 * @param  friendlyURLMap the web content article's locales and localized
	 *         friendly URLs
	 * @param  content the HTML content wrapped in XML. For more information,
	 *         see the content example in the {@link #addArticle(long, long,
	 *         long, long, long, String, boolean, double, Map, Map, String,
	 *         String, String, String, int, int, int, int, int, int, int, int,
	 *         int, int, boolean, int, int, int, int, int, boolean, boolean,
	 *         boolean, String, File, Map, String, ServiceContext)} description.
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure, if the article is related to a DDM structure, or
	 *         <code>null</code> otherwise
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  layoutUuid the unique string identifying the web content
	 *         article's display page
	 * @param  displayDateMonth the month the web content article is set to
	 *         display
	 * @param  displayDateDay the calendar day the web content article is set to
	 *         display
	 * @param  displayDateYear the year the web content article is set to
	 *         display
	 * @param  displayDateHour the hour the web content article is set to
	 *         display
	 * @param  displayDateMinute the minute the web content article is set to
	 *         display
	 * @param  expirationDateMonth the month the web content article is set to
	 *         expire
	 * @param  expirationDateDay the calendar day the web content article is set
	 *         to expire
	 * @param  expirationDateYear the year the web content article is set to
	 *         expire
	 * @param  expirationDateHour the hour the web content article is set to
	 *         expire
	 * @param  expirationDateMinute the minute the web content article is set to
	 *         expire
	 * @param  neverExpire whether the web content article is not set to auto
	 *         expire
	 * @param  reviewDateMonth the month the web content article is set for
	 *         review
	 * @param  reviewDateDay the calendar day the web content article is set for
	 *         review
	 * @param  reviewDateYear the year the web content article is set for review
	 * @param  reviewDateHour the hour the web content article is set for review
	 * @param  reviewDateMinute the minute the web content article is set for
	 *         review
	 * @param  neverReview whether the web content article is not set for review
	 * @param  indexable whether the web content is searchable
	 * @param  smallImage whether to update web content article's a small image.
	 *         A file must be passed in as <code>smallImageFile</code> value,
	 *         otherwise the current small image is deleted.
	 * @param  smallImageURL the web content article's small image URL
	 *         (optionally <code>null</code>)
	 * @param  smallImageFile the web content article's new small image file
	 *         (optionally <code>null</code>). Must pass in
	 *         <code>smallImage</code> value of <code>true</code> to replace the
	 *         article's small image file.
	 * @param  images the web content's images (optionally <code>null</code>)
	 * @param  articleURL the web content article's accessible URL (optionally
	 *         <code>null</code>)
	 * @param  serviceContext the service context to be applied. Can set the
	 *         modification date, expando bridge attributes, asset category IDs,
	 *         asset tag names, asset link entry IDs, asset priority, workflow
	 *         actions, URL title , and can set whether to add the default
	 *         command update for the web content article. With respect to
	 *         social activities, by setting the service context's command to
	 *         {@link Constants#UPDATE}, the invocation is considered a web
	 *         content update activity; otherwise it is considered a web content
	 *         add activity.
	 * @return the updated web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle updateArticle(
			long userId, long groupId, long folderId, String articleId,
			double version, Map<Locale, String> titleMap,
			Map<Locale, String> descriptionMap,
			Map<Locale, String> friendlyURLMap, String content,
			String ddmStructureKey, String ddmTemplateKey, String layoutUuid,
			int displayDateMonth, int displayDateDay, int displayDateYear,
			int displayDateHour, int displayDateMinute, int expirationDateMonth,
			int expirationDateDay, int expirationDateYear,
			int expirationDateHour, int expirationDateMinute,
			boolean neverExpire, int reviewDateMonth, int reviewDateDay,
			int reviewDateYear, int reviewDateHour, int reviewDateMinute,
			boolean neverReview, boolean indexable, boolean smallImage,
			String smallImageURL, File smallImageFile,
			Map<String, byte[]> images, String articleURL,
			ServiceContext serviceContext)
		throws PortalException {

		// Article

		User user = userLocalService.getUser(userId);
		articleId = StringUtil.toUpperCase(StringUtil.trim(articleId));

		byte[] smallImageBytes = null;

		try {
			smallImageBytes = FileUtil.getBytes(smallImageFile);
		}
		catch (IOException ioe) {
		}

		JournalArticle latestArticle = getLatestArticle(
			groupId, articleId, WorkflowConstants.STATUS_ANY);

		JournalArticle article = latestArticle;

		boolean imported = ExportImportThreadLocal.isImportInProcess();

		boolean addNewVersion = false;

		if (imported) {
			article = getArticle(groupId, articleId, version);
		}
		else {
			double latestArticleVersion = latestArticle.getVersion();

			if ((version > 0) && (version != latestArticleVersion)) {
				StringBundler sb = new StringBundler(4);

				sb.append("Version ");
				sb.append(version);
				sb.append(" is not the same as ");
				sb.append(latestArticleVersion);

				throw new ArticleVersionException(sb.toString());
			}

			serviceContext.validateModifiedDate(
				latestArticle, ArticleVersionException.class);

			if (latestArticle.isApproved() || latestArticle.isExpired() ||
				latestArticle.isScheduled()) {

				addNewVersion = true;

				version = getNextVersion(article);
			}
		}

		Date displayDate = _portal.getDate(
			displayDateMonth, displayDateDay, displayDateYear, displayDateHour,
			displayDateMinute, user.getTimeZone(), null);

		Date expirationDate = null;
		Date reviewDate = null;

		if (!neverExpire) {
			expirationDate = _portal.getDate(
				expirationDateMonth, expirationDateDay, expirationDateYear,
				expirationDateHour, expirationDateMinute, user.getTimeZone(),
				ArticleExpirationDateException.class);
		}

		if (!neverReview) {
			reviewDate = _portal.getDate(
				reviewDateMonth, reviewDateDay, reviewDateYear, reviewDateHour,
				reviewDateMinute, user.getTimeZone(),
				ArticleReviewDateException.class);
		}

		Date now = new Date();

		boolean expired = false;

		if ((expirationDate != null) && expirationDate.before(now)) {
			expired = true;
		}

		sanitize(
			user.getCompanyId(), groupId, userId, article.getClassPK(),
			descriptionMap);

		boolean validate = !ExportImportThreadLocal.isImportInProcess();

		if (validate) {
			validate(
				user.getCompanyId(), groupId, latestArticle.getClassNameId(),
				titleMap, content, ddmStructureKey, ddmTemplateKey, displayDate,
				expirationDate, smallImage, smallImageURL, smallImageFile,
				smallImageBytes, serviceContext);

			try {
				validateReferences(
					groupId, ddmStructureKey, ddmTemplateKey, layoutUuid,
					smallImage, smallImageURL, smallImageBytes,
					latestArticle.getSmallImageId(), content);
			}
			catch (ExportImportContentValidationException eicve) {
				eicve.setStagedModelClassName(JournalArticle.class.getName());
				eicve.setStagedModelPrimaryKeyObj(articleId);

				throw eicve;
			}
		}

		if (addNewVersion) {
			long id = counterLocalService.increment();

			article = journalArticlePersistence.create(id);

			article.setResourcePrimKey(latestArticle.getResourcePrimKey());
			article.setGroupId(latestArticle.getGroupId());
			article.setCompanyId(latestArticle.getCompanyId());
			article.setUserId(user.getUserId());
			article.setUserName(user.getFullName());
			article.setCreateDate(latestArticle.getCreateDate());
			article.setClassNameId(latestArticle.getClassNameId());
			article.setClassPK(latestArticle.getClassPK());
			article.setArticleId(articleId);
			article.setVersion(version);
			article.setSmallImageId(latestArticle.getSmallImageId());

			serviceContext.setAttribute("version", version);

			_addArticleLocalizedFields(
				article.getCompanyId(), article.getId(), titleMap,
				descriptionMap);
		}
		else {
			_updateArticleLocalizedFields(
				article.getCompanyId(), article.getId(), titleMap,
				descriptionMap);
		}

		Locale locale = getArticleDefaultLocale(content);

		Map<String, String> urlTitleMap = _getURLTitleMap(
			groupId, article.getResourcePrimKey(), friendlyURLMap, titleMap);

		String urlTitle = urlTitleMap.get(LocaleUtil.toLanguageId(locale));

		if (Validator.isNull(urlTitle)) {
			throw new ArticleFriendlyURLException();
		}

		content = format(user, groupId, article, content);
		content = _replaceTempImages(article, content);

		article.setFolderId(folderId);
		article.setTreePath(article.buildTreePath());
		article.setUrlTitle(urlTitle);
		article.setContent(content);
		article.setDDMStructureKey(ddmStructureKey);
		article.setDDMTemplateKey(ddmTemplateKey);
		article.setDefaultLanguageId(LocaleUtil.toLanguageId(locale));
		article.setLayoutUuid(layoutUuid);
		article.setDisplayDate(displayDate);
		article.setExpirationDate(expirationDate);
		article.setReviewDate(reviewDate);
		article.setIndexable(indexable);
		article.setSmallImage(smallImage);

		if (smallImage) {
			if ((smallImageFile != null) && (smallImageBytes != null) &&
				(article.getSmallImageId() <= 0)) {

				article.setSmallImageId(counterLocalService.increment());
			}
		}
		else {
			article.setSmallImageId(0);
		}

		article.setSmallImageURL(smallImageURL);

		if (latestArticle.isPending()) {
			article.setStatus(latestArticle.getStatus());
		}
		else if (!expired) {
			article.setStatus(WorkflowConstants.STATUS_DRAFT);
		}
		else {
			article.setStatus(WorkflowConstants.STATUS_EXPIRED);
		}

		ExpandoBridgeUtil.setExpandoBridgeAttributes(
			latestArticle.getExpandoBridge(), article.getExpandoBridge(),
			serviceContext);

		article = journalArticlePersistence.update(article);

		// Friendly URLs

		updateFriendlyURLs(article, urlTitleMap, serviceContext);

		// Asset

		if (hasModifiedLatestApprovedVersion(groupId, articleId, version)) {
			updateAsset(
				userId, article, serviceContext.getAssetCategoryIds(),
				serviceContext.getAssetTagNames(),
				serviceContext.getAssetLinkEntryIds(),
				serviceContext.getAssetPriority());
		}

		// Dynamic data mapping

		updateDDMLinks(
			article.getId(), groupId, ddmStructureKey, ddmTemplateKey,
			addNewVersion);

		// Small image

		saveImages(
			smallImage, article.getSmallImageId(), smallImageFile,
			smallImageBytes);

		// Email and workflow

		if (expired && imported) {
			article = updateStatus(
				userId, article, article.getStatus(), articleURL,
				serviceContext, new HashMap<>());
		}

		if (serviceContext.getWorkflowAction() ==
				WorkflowConstants.ACTION_PUBLISH) {

			articleURL = buildArticleURL(
				articleURL, groupId, folderId, articleId);

			serviceContext.setAttribute("articleURL", articleURL);

			sendEmail(article, articleURL, "requested", serviceContext);

			startWorkflowInstance(userId, article, serviceContext);
		}

		return article;
	}

	/**
	 * Updates the web content article matching the version, replacing its
	 * folder, title, description, content, and layout UUID.
	 *
	 * @param  userId the primary key of the user updating the web content
	 *         article
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article folder
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @param  titleMap the web content article's locales and localized titles
	 * @param  descriptionMap the web content article's locales and localized
	 *         descriptions
	 * @param  content the HTML content wrapped in XML. For more information,
	 *         see the content example in the {@link #addArticle(long, long,
	 *         long, long, long, String, boolean, double, Map, Map, String,
	 *         String, String, String, int, int, int, int, int, int, int, int,
	 *         int, int, boolean, int, int, int, int, int, boolean, boolean,
	 *         boolean, String, File, Map, String, ServiceContext)} description.
	 * @param  layoutUuid the unique string identifying the web content
	 *         article's display page
	 * @param  serviceContext the service context to be applied. Can set the
	 *         modification date, expando bridge attributes, asset category IDs,
	 *         asset tag names, asset link entry IDs, asset priority, workflow
	 *         actions, URL title, and can set whether to add the default
	 *         command update for the web content article. With respect to
	 *         social activities, by setting the service context's command to
	 *         {@link Constants#UPDATE}, the invocation is considered a web
	 *         content update activity; otherwise it is considered a web content
	 *         add activity.
	 * @return the updated web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle updateArticle(
			long userId, long groupId, long folderId, String articleId,
			double version, Map<Locale, String> titleMap,
			Map<Locale, String> descriptionMap, String content,
			String layoutUuid, ServiceContext serviceContext)
		throws PortalException {

		User user = userLocalService.getUser(userId);

		JournalArticle article = journalArticlePersistence.findByG_A_V(
			groupId, articleId, version);

		Date displayDate = article.getDisplayDate();

		int displayDateMonth = 0;
		int displayDateDay = 0;
		int displayDateYear = 0;
		int displayDateHour = 0;
		int displayDateMinute = 0;

		if (displayDate != null) {
			Calendar displayCal = CalendarFactoryUtil.getCalendar(
				user.getTimeZone());

			displayCal.setTime(displayDate);

			displayDateMonth = displayCal.get(Calendar.MONTH);
			displayDateDay = displayCal.get(Calendar.DATE);
			displayDateYear = displayCal.get(Calendar.YEAR);
			displayDateHour = displayCal.get(Calendar.HOUR);
			displayDateMinute = displayCal.get(Calendar.MINUTE);

			if (displayCal.get(Calendar.AM_PM) == Calendar.PM) {
				displayDateHour += 12;
			}
		}

		Date expirationDate = article.getExpirationDate();

		int expirationDateMonth = 0;
		int expirationDateDay = 0;
		int expirationDateYear = 0;
		int expirationDateHour = 0;
		int expirationDateMinute = 0;
		boolean neverExpire = true;

		if (expirationDate != null) {
			Calendar expirationCal = CalendarFactoryUtil.getCalendar(
				user.getTimeZone());

			expirationCal.setTime(expirationDate);

			expirationDateMonth = expirationCal.get(Calendar.MONTH);
			expirationDateDay = expirationCal.get(Calendar.DATE);
			expirationDateYear = expirationCal.get(Calendar.YEAR);
			expirationDateHour = expirationCal.get(Calendar.HOUR);
			expirationDateMinute = expirationCal.get(Calendar.MINUTE);

			neverExpire = false;

			if (expirationCal.get(Calendar.AM_PM) == Calendar.PM) {
				expirationDateHour += 12;
			}
		}

		Date reviewDate = article.getReviewDate();

		int reviewDateMonth = 0;
		int reviewDateDay = 0;
		int reviewDateYear = 0;
		int reviewDateHour = 0;
		int reviewDateMinute = 0;
		boolean neverReview = true;

		if (reviewDate != null) {
			Calendar reviewCal = CalendarFactoryUtil.getCalendar(
				user.getTimeZone());

			reviewCal.setTime(reviewDate);

			reviewDateMonth = reviewCal.get(Calendar.MONTH);
			reviewDateDay = reviewCal.get(Calendar.DATE);
			reviewDateYear = reviewCal.get(Calendar.YEAR);
			reviewDateHour = reviewCal.get(Calendar.HOUR);
			reviewDateMinute = reviewCal.get(Calendar.MINUTE);

			neverReview = false;

			if (reviewCal.get(Calendar.AM_PM) == Calendar.PM) {
				reviewDateHour += 12;
			}
		}

		return journalArticleLocalService.updateArticle(
			userId, groupId, folderId, articleId, version, titleMap,
			descriptionMap, content, article.getDDMStructureKey(),
			article.getDDMTemplateKey(), layoutUuid, displayDateMonth,
			displayDateDay, displayDateYear, displayDateHour, displayDateMinute,
			expirationDateMonth, expirationDateDay, expirationDateYear,
			expirationDateHour, expirationDateMinute, neverExpire,
			reviewDateMonth, reviewDateDay, reviewDateYear, reviewDateHour,
			reviewDateMinute, neverReview, article.isIndexable(),
			article.isSmallImage(), article.getSmallImageURL(), null, null,
			null, serviceContext);
	}

	/**
	 * Updates the web content article with additional parameters. All
	 * scheduling parameters (display date, expiration date, and review date)
	 * use the current user's timezone.
	 *
	 * @param  userId the primary key of the user updating the web content
	 *         article
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article folder
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @param  titleMap the web content article's locales and localized titles
	 * @param  descriptionMap the web content article's locales and localized
	 *         descriptions
	 * @param  content the HTML content wrapped in XML. For more information,
	 *         see the content example in the {@link #addArticle(long, long,
	 *         long, long, long, String, boolean, double, Map, Map, String,
	 *         String, String, String, int, int, int, int, int, int, int, int,
	 *         int, int, boolean, int, int, int, int, int, boolean, boolean,
	 *         boolean, String, File, Map, String, ServiceContext)} description.
	 * @param  ddmStructureKey the primary key of the web content article's DDM
	 *         structure, if the article is related to a DDM structure, or
	 *         <code>null</code> otherwise
	 * @param  ddmTemplateKey the primary key of the web content article's DDM
	 *         template
	 * @param  layoutUuid the unique string identifying the web content
	 *         article's display page
	 * @param  displayDateMonth the month the web content article is set to
	 *         display
	 * @param  displayDateDay the calendar day the web content article is set to
	 *         display
	 * @param  displayDateYear the year the web content article is set to
	 *         display
	 * @param  displayDateHour the hour the web content article is set to
	 *         display
	 * @param  displayDateMinute the minute the web content article is set to
	 *         display
	 * @param  expirationDateMonth the month the web content article is set to
	 *         expire
	 * @param  expirationDateDay the calendar day the web content article is set
	 *         to expire
	 * @param  expirationDateYear the year the web content article is set to
	 *         expire
	 * @param  expirationDateHour the hour the web content article is set to
	 *         expire
	 * @param  expirationDateMinute the minute the web content article is set to
	 *         expire
	 * @param  neverExpire whether the web content article is not set to auto
	 *         expire
	 * @param  reviewDateMonth the month the web content article is set for
	 *         review
	 * @param  reviewDateDay the calendar day the web content article is set for
	 *         review
	 * @param  reviewDateYear the year the web content article is set for review
	 * @param  reviewDateHour the hour the web content article is set for review
	 * @param  reviewDateMinute the minute the web content article is set for
	 *         review
	 * @param  neverReview whether the web content article is not set for review
	 * @param  indexable whether the web content is searchable
	 * @param  smallImage whether to update web content article's a small image.
	 *         A file must be passed in as <code>smallImageFile</code> value,
	 *         otherwise the current small image is deleted.
	 * @param  smallImageURL the web content article's small image URL
	 *         (optionally <code>null</code>)
	 * @param  smallImageFile the web content article's new small image file
	 *         (optionally <code>null</code>). Must pass in
	 *         <code>smallImage</code> value of <code>true</code> to replace the
	 *         article's small image file.
	 * @param  images the web content's images (optionally <code>null</code>)
	 * @param  articleURL the web content article's accessible URL (optionally
	 *         <code>null</code>)
	 * @param  serviceContext the service context to be applied. Can set the
	 *         modification date, expando bridge attributes, asset category IDs,
	 *         asset tag names, asset link entry IDs, asset priority, workflow
	 *         actions, URL title , and can set whether to add the default
	 *         command update for the web content article. With respect to
	 *         social activities, by setting the service context's command to
	 *         {@link Constants#UPDATE}, the invocation is considered a web
	 *         content update activity; otherwise it is considered a web content
	 *         add activity.
	 * @return the updated web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle updateArticle(
			long userId, long groupId, long folderId, String articleId,
			double version, Map<Locale, String> titleMap,
			Map<Locale, String> descriptionMap, String content,
			String ddmStructureKey, String ddmTemplateKey, String layoutUuid,
			int displayDateMonth, int displayDateDay, int displayDateYear,
			int displayDateHour, int displayDateMinute, int expirationDateMonth,
			int expirationDateDay, int expirationDateYear,
			int expirationDateHour, int expirationDateMinute,
			boolean neverExpire, int reviewDateMonth, int reviewDateDay,
			int reviewDateYear, int reviewDateHour, int reviewDateMinute,
			boolean neverReview, boolean indexable, boolean smallImage,
			String smallImageURL, File smallImageFile,
			Map<String, byte[]> images, String articleURL,
			ServiceContext serviceContext)
		throws PortalException {

		JournalArticle article = getLatestArticle(
			groupId, articleId, WorkflowConstants.STATUS_ANY);

		Map<Locale, String> friendlyURLMap = new HashMap<>();

		for (Map.Entry<Locale, String> entry : titleMap.entrySet()) {
			String title = entry.getValue();

			if (Validator.isNull(title)) {
				continue;
			}

			String urlTitle = friendlyURLEntryLocalService.getUniqueUrlTitle(
				groupId,
				classNameLocalService.getClassNameId(JournalArticle.class),
				article.getResourcePrimKey(), title);

			friendlyURLMap.put(entry.getKey(), urlTitle);
		}

		return journalArticleLocalService.updateArticle(
			userId, groupId, folderId, articleId, version, titleMap,
			descriptionMap, friendlyURLMap, content, ddmStructureKey,
			ddmTemplateKey, layoutUuid, displayDateMonth, displayDateDay,
			displayDateYear, displayDateHour, displayDateMinute,
			expirationDateMonth, expirationDateDay, expirationDateYear,
			expirationDateHour, expirationDateMinute, neverExpire,
			reviewDateMonth, reviewDateDay, reviewDateYear, reviewDateHour,
			reviewDateMinute, neverReview, indexable, smallImage, smallImageURL,
			null, null, null, serviceContext);
	}

	/**
	 * Updates the web content article matching the version, replacing its
	 * folder and content.
	 *
	 * @param  userId the primary key of the user updating the web content
	 *         article
	 * @param  groupId the primary key of the web content article's group
	 * @param  folderId the primary key of the web content article folder
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @param  content the HTML content wrapped in XML. For more information,
	 *         see the content example in the {@link #addArticle(long, long,
	 *         long, long, long, String, boolean, double, Map, Map, String,
	 *         String, String, String, int, int, int, int, int, int, int, int,
	 *         int, int, boolean, int, int, int, int, int, boolean, boolean,
	 *         boolean, String, File, Map, String, ServiceContext)} description.
	 * @param  serviceContext the service context to be applied. Can set the
	 *         modification date, expando bridge attributes, asset category IDs,
	 *         asset tag names, asset link entry IDs, asset priority, workflow
	 *         actions, URL title, and can set whether to add the default
	 *         command update for the web content article. With respect to
	 *         social activities, by setting the service context's command to
	 *         {@link Constants#UPDATE}, the invocation is considered a web
	 *         content update activity; otherwise it is considered a web content
	 *         add activity.
	 * @return the updated web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle updateArticle(
			long userId, long groupId, long folderId, String articleId,
			double version, String content, ServiceContext serviceContext)
		throws PortalException {

		JournalArticle article = journalArticlePersistence.findByG_A_V(
			groupId, articleId, version);

		return journalArticleLocalService.updateArticle(
			userId, groupId, folderId, articleId, version,
			article.getTitleMap(), article.getDescriptionMap(), content,
			article.getLayoutUuid(), serviceContext);
	}

	/**
	 * Updates the URL title of the web content article.
	 *
	 * @param  id the primary key of the web content article
	 * @param  urlTitle the web content article's URL title
	 * @return the updated web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle updateArticle(long id, String urlTitle)
		throws PortalException {

		JournalArticle article = journalArticlePersistence.findByPrimaryKey(id);

		article.setUrlTitle(urlTitle);

		journalArticlePersistence.update(article);

		return article;
	}

	@Override
	public JournalArticle updateArticleDefaultValues(
			long userId, long groupId, String articleId,
			Map<Locale, String> titleMap, Map<Locale, String> descriptionMap,
			String content, String ddmStructureKey, String ddmTemplateKey,
			String layoutUuid, boolean indexable, boolean smallImage,
			String smallImageURL, File smallImageFile,
			ServiceContext serviceContext)
		throws PortalException {

		// Article

		User user = userLocalService.getUser(userId);
		articleId = StringUtil.toUpperCase(StringUtil.trim(articleId));

		byte[] smallImageBytes = null;

		try {
			smallImageBytes = FileUtil.getBytes(smallImageFile);
		}
		catch (IOException ioe) {
		}

		JournalArticle article = getArticle(groupId, articleId);

		serviceContext.validateModifiedDate(
			article, ArticleVersionException.class);

		sanitize(
			user.getCompanyId(), groupId, userId, article.getClassPK(),
			descriptionMap);

		validate(
			user.getCompanyId(), groupId, article.getClassNameId(), titleMap,
			content, ddmStructureKey, ddmTemplateKey, null, null, smallImage,
			smallImageURL, smallImageFile, smallImageBytes, serviceContext);

		_updateArticleLocalizedFields(
			article.getCompanyId(), article.getId(), titleMap, descriptionMap);

		content = format(user, groupId, article, content);
		content = _replaceTempImages(article, content);

		article.setContent(content);

		article.setDDMStructureKey(ddmStructureKey);
		article.setDDMTemplateKey(ddmTemplateKey);

		Locale locale = getArticleDefaultLocale(content);

		article.setDefaultLanguageId(LocaleUtil.toLanguageId(locale));

		article.setLayoutUuid(layoutUuid);
		article.setIndexable(indexable);
		article.setSmallImage(smallImage);

		if (smallImage) {
			if ((smallImageFile != null) && (smallImageBytes != null) &&
				(article.getSmallImageId() <= 0)) {

				article.setSmallImageId(counterLocalService.increment());
			}
		}
		else {
			article.setSmallImageId(0);
		}

		article.setSmallImageURL(smallImageURL);
		article.setStatus(WorkflowConstants.STATUS_APPROVED);

		ExpandoBridgeUtil.setExpandoBridgeAttributes(
			article.getExpandoBridge(), article.getExpandoBridge(),
			serviceContext);

		journalArticlePersistence.update(article);

		// Asset

		updateAsset(
			userId, article, serviceContext.getAssetCategoryIds(),
			serviceContext.getAssetTagNames(),
			serviceContext.getAssetLinkEntryIds(),
			serviceContext.getAssetPriority());

		// Dynamic data mapping

		updateDDMStructurePredefinedValues(
			article.getClassPK(), content, serviceContext);

		// Small image

		saveImages(
			smallImage, article.getSmallImageId(), smallImageFile,
			smallImageBytes);

		return journalArticlePersistence.findByPrimaryKey(article.getId());
	}

	/**
	 * Updates the translation of the web content article.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @param  locale the locale of the web content article's display template
	 * @param  title the translated web content article title
	 * @param  description the translated web content article description
	 * @param  content the HTML content wrapped in XML. For more information,
	 *         see the content example in the {@link #addArticle(long, long,
	 *         long, long, long, String, boolean, double, Map, Map, String,
	 *         String, String, String, int, int, int, int, int, int, int, int,
	 *         int, int, boolean, int, int, int, int, int, boolean, boolean,
	 *         boolean, String, File, Map, String, ServiceContext)} description.
	 * @param  images the web content's images
	 * @param  serviceContext the service context to be applied. Can set the
	 *         modification date and URL title for the web content article.
	 * @return the updated web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle updateArticleTranslation(
			long groupId, String articleId, double version, Locale locale,
			String title, String description, String content,
			Map<String, byte[]> images, ServiceContext serviceContext)
		throws PortalException {

		validateContent(content);

		JournalArticle oldArticle = getLatestArticle(
			groupId, articleId, WorkflowConstants.STATUS_ANY);

		double oldVersion = oldArticle.getVersion();

		if ((version > 0) && (version != oldVersion)) {
			StringBundler sb = new StringBundler(4);

			sb.append("Version ");
			sb.append(version);
			sb.append(" is not the same as ");
			sb.append(oldVersion);

			throw new ArticleVersionException(sb.toString());
		}

		boolean incrementVersion = false;

		if (oldArticle.isApproved() || oldArticle.isExpired()) {
			incrementVersion = true;
		}

		if (serviceContext != null) {
			serviceContext.validateModifiedDate(
				oldArticle, ArticleVersionException.class);
		}

		JournalArticle article = null;

		User user = userLocalService.fetchUser(oldArticle.getUserId());

		if (user == null) {
			user = userLocalService.getDefaultUser(oldArticle.getCompanyId());
		}

		if (incrementVersion) {
			double newVersion = getNextVersion(oldArticle);

			long id = counterLocalService.increment();

			article = journalArticlePersistence.create(id);

			article.setResourcePrimKey(oldArticle.getResourcePrimKey());
			article.setGroupId(oldArticle.getGroupId());
			article.setCompanyId(oldArticle.getCompanyId());
			article.setUserId(oldArticle.getUserId());
			article.setUserName(user.getFullName());
			article.setCreateDate(oldArticle.getCreateDate());
			article.setFolderId(oldArticle.getFolderId());
			article.setClassNameId(oldArticle.getClassNameId());
			article.setClassPK(oldArticle.getClassPK());
			article.setArticleId(articleId);
			article.setVersion(newVersion);
			article.setUrlTitle(
				getUniqueUrlTitle(
					id, groupId, articleId, title, oldArticle.getUrlTitle(),
					serviceContext));
			article.setDDMStructureKey(oldArticle.getDDMStructureKey());
			article.setDDMTemplateKey(oldArticle.getDDMTemplateKey());
			article.setDefaultLanguageId(
				LocaleUtil.toLanguageId(getArticleDefaultLocale(content)));
			article.setLayoutUuid(oldArticle.getLayoutUuid());
			article.setDisplayDate(oldArticle.getDisplayDate());
			article.setExpirationDate(oldArticle.getExpirationDate());
			article.setReviewDate(oldArticle.getReviewDate());
			article.setIndexable(oldArticle.isIndexable());
			article.setSmallImage(oldArticle.isSmallImage());
			article.setSmallImageId(oldArticle.getSmallImageId());

			if (article.getSmallImageId() == 0) {
				article.setSmallImageId(counterLocalService.increment());
			}

			article.setSmallImageURL(oldArticle.getSmallImageURL());

			article.setStatus(WorkflowConstants.STATUS_DRAFT);
			article.setStatusDate(new Date());

			ExpandoBridgeUtil.copyExpandoBridgeAttributes(
				oldArticle.getExpandoBridge(), article.getExpandoBridge());

			// Article localization

			_addArticleLocalizedFields(
				article.getCompanyId(), article.getId(),
				oldArticle.getTitleMap(), oldArticle.getDescriptionMap());

			// Dynamic data mapping

			updateDDMLinks(
				id, groupId, oldArticle.getDDMStructureKey(),
				oldArticle.getDDMTemplateKey(), true);
		}
		else {
			article = oldArticle;
		}

		_updateArticleLocalizedFields(
			article.getCompanyId(), article.getId(), title, description,
			LocaleUtil.toLanguageId(locale));

		content = format(user, groupId, article, content);
		content = _replaceTempImages(article, content);

		article.setContent(content);

		journalArticlePersistence.update(article);

		return article;
	}

	/**
	 * Updates the web content article's asset with the new asset categories,
	 * tag names, and link entries, removing and adding them as necessary.
	 *
	 * @param      userId the primary key of the user updating the web content
	 *             article's asset
	 * @param      article the web content article
	 * @param      assetCategoryIds the primary keys of the new asset categories
	 * @param      assetTagNames the new asset tag names
	 * @param      assetLinkEntryIds the primary keys of the new asset link
	 *             entries
	 * @throws     PortalException if a portal exception occurred
	 * @deprecated As of Wilberforce (7.0.x), replaced by {@link
	 *             #updateAsset(long, JournalArticle, long[], String[], long[],
	 *             Double)}
	 */
	@Deprecated
	@Override
	public void updateAsset(
			long userId, JournalArticle article, long[] assetCategoryIds,
			String[] assetTagNames, long[] assetLinkEntryIds)
		throws PortalException {

		updateAsset(
			userId, article, assetCategoryIds, assetTagNames, assetLinkEntryIds,
			null);
	}

	/**
	 * Updates the web content article's asset with the new asset categories,
	 * tag names, and link entries, removing and adding them as necessary.
	 *
	 * @param  userId the primary key of the user updating the web content
	 *         article's asset
	 * @param  article the web content article
	 * @param  assetCategoryIds the primary keys of the new asset categories
	 * @param  assetTagNames the new asset tag names
	 * @param  assetLinkEntryIds the primary keys of the new asset link entries
	 * @param  priority the priority of the asset
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public void updateAsset(
			long userId, JournalArticle article, long[] assetCategoryIds,
			String[] assetTagNames, long[] assetLinkEntryIds, Double priority)
		throws PortalException {

		boolean visible = article.isApproved();

		if (article.getClassNameId() !=
				JournalArticleConstants.CLASSNAME_ID_DEFAULT) {

			visible = false;
		}

		boolean addDraftAssetEntry = false;

		if (!article.isApproved() &&
			(article.getVersion() != JournalArticleConstants.VERSION_DEFAULT)) {

			int approvedArticlesCount = journalArticlePersistence.countByG_A_ST(
				article.getGroupId(), article.getArticleId(),
				JournalArticleConstants.ASSET_ENTRY_CREATION_STATUSES);

			if (approvedArticlesCount > 0) {
				addDraftAssetEntry = true;
			}
		}

		AssetEntry assetEntry = null;

		String title = article.getTitleMapAsXML();
		String description = article.getDescriptionMapAsXML();

		if (addDraftAssetEntry) {
			assetEntry = assetEntryLocalService.updateEntry(
				userId, article.getGroupId(), article.getCreateDate(),
				article.getModifiedDate(), JournalArticle.class.getName(),
				article.getPrimaryKey(), article.getUuid(),
				getClassTypeId(article), assetCategoryIds, assetTagNames,
				isListable(article), false, null, null, null,
				article.getExpirationDate(), ContentTypes.TEXT_HTML, title,
				description, description, null, article.getLayoutUuid(), 0, 0,
				priority);
		}
		else {
			JournalArticleResource journalArticleResource =
				_journalArticleResourceLocalService.getArticleResource(
					article.getResourcePrimKey());

			Date publishDate = null;

			if (article.isApproved()) {
				publishDate = article.getDisplayDate();
			}

			assetEntry = assetEntryLocalService.updateEntry(
				userId, article.getGroupId(), article.getCreateDate(),
				article.getModifiedDate(), JournalArticle.class.getName(),
				journalArticleResource.getResourcePrimKey(),
				journalArticleResource.getUuid(), getClassTypeId(article),
				assetCategoryIds, assetTagNames, isListable(article), visible,
				null, null, publishDate, article.getExpirationDate(),
				ContentTypes.TEXT_HTML, title, description, description, null,
				article.getLayoutUuid(), 0, 0, priority);
		}

		assetLinkLocalService.updateLinks(
			userId, assetEntry.getEntryId(), assetLinkEntryIds,
			AssetLinkConstants.TYPE_RELATED);
	}

	/**
	 * Updates the web content article matching the group, article ID, and
	 * version, replacing its content.
	 *
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @param  content the HTML content wrapped in XML. For more information,
	 *         see the content example in the {@link #addArticle(long, long,
	 *         long, long, long, String, boolean, double, Map, Map, String,
	 *         String, String, String, int, int, int, int, int, int, int, int,
	 *         int, int, boolean, int, int, int, int, int, boolean, boolean,
	 *         boolean, String, File, Map, String, ServiceContext)} description.
	 * @return the updated web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle updateContent(
			long groupId, String articleId, double version, String content)
		throws PortalException {

		JournalArticle article = journalArticlePersistence.findByG_A_V(
			groupId, articleId, version);

		article.setContent(content);

		journalArticlePersistence.update(article);

		return article;
	}

	/**
	 * Updates the web content articles matching the group, class name ID, and
	 * DDM template key, replacing the DDM template key with a new one.
	 *
	 * @param groupId the primary key of the web content article's group
	 * @param classNameId the primary key of the DDMStructure class if the web
	 *        content article is related to a DDM structure, the primary key of
	 *        the class name associated with the article, or
	 *        JournalArticleConstants.CLASSNAME_ID_DEFAULT in the journal-api
	 *        module otherwise
	 * @param oldDDMTemplateKey the primary key of the web content article's old
	 *        DDM template
	 * @param newDDMTemplateKey the primary key of the web content article's new
	 *        DDM template
	 */
	@Override
	public void updateDDMTemplateKey(
		long groupId, long classNameId, String oldDDMTemplateKey,
		String newDDMTemplateKey) {

		List<JournalArticle> articles =
			journalArticlePersistence.findByG_C_DDMTK(
				groupId, classNameId, oldDDMTemplateKey);

		for (JournalArticle article : articles) {
			article.setDDMTemplateKey(newDDMTemplateKey);

			journalArticlePersistence.update(article);
		}
	}

	/**
	 * Updates the workflow status of the web content article.
	 *
	 * @param  userId the primary key of the user updating the web content
	 *         article's status
	 * @param  article the web content article
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  articleURL the web content article's accessible URL
	 * @param  serviceContext the service context to be applied. Can set the
	 *         modification date, status date, and portlet preferences. With
	 *         respect to social activities, by setting the service context's
	 *         command to {@link Constants#UPDATE}, the invocation is considered
	 *         a web content update activity; otherwise it is considered a web
	 *         content add activity.
	 * @param  workflowContext the web content article's configured workflow
	 *         context
	 * @return the updated web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Indexable(type = IndexableType.REINDEX)
	@Override
	public JournalArticle updateStatus(
			long userId, JournalArticle article, int status, String articleURL,
			ServiceContext serviceContext,
			Map<String, Serializable> workflowContext)
		throws PortalException {

		// Article

		User user = userLocalService.getUser(userId);

		Date now = new Date();

		if ((status == WorkflowConstants.STATUS_APPROVED) &&
			(article.getDisplayDate() != null) &&
			now.before(article.getDisplayDate())) {

			status = WorkflowConstants.STATUS_SCHEDULED;
		}

		int oldStatus = article.getStatus();

		Date modifiedDate = serviceContext.getModifiedDate(now);

		article.setModifiedDate(modifiedDate);

		if (status == WorkflowConstants.STATUS_APPROVED) {
			Date expirationDate = article.getExpirationDate();

			if ((expirationDate != null) && expirationDate.before(now)) {
				article.setExpirationDate(null);
			}
		}

		if (status == WorkflowConstants.STATUS_EXPIRED) {
			article.setExpirationDate(now);
		}

		article.setStatus(status);
		article.setStatusByUserId(user.getUserId());
		article.setStatusByUserName(user.getFullName());
		article.setStatusDate(modifiedDate);

		article = journalArticlePersistence.update(article);

		if (isExpireAllArticleVersions(article.getCompanyId())) {
			article = setArticlesExpirationDate(article);
		}

		if (hasModifiedLatestApprovedVersion(
				article.getGroupId(), article.getArticleId(),
				article.getVersion())) {

			if (status == WorkflowConstants.STATUS_APPROVED) {
				updateUrlTitles(
					article.getGroupId(), article.getArticleId(),
					article.getUrlTitle());

				// Asset

				String title = article.getTitleMapAsXML();

				if ((oldStatus != WorkflowConstants.STATUS_APPROVED) &&
					(article.getVersion() !=
						JournalArticleConstants.VERSION_DEFAULT)) {

					AssetEntry draftAssetEntry =
						assetEntryLocalService.fetchEntry(
							JournalArticle.class.getName(),
							article.getPrimaryKey());

					if (draftAssetEntry != null) {
						long[] assetCategoryIds =
							draftAssetEntry.getCategoryIds();
						String[] assetTagNames = draftAssetEntry.getTagNames();

						List<AssetLink> assetLinks =
							assetLinkLocalService.getDirectLinks(
								draftAssetEntry.getEntryId(),
								AssetLinkConstants.TYPE_RELATED, false);

						long[] assetLinkEntryIds = ListUtil.toLongArray(
							assetLinks, AssetLink.ENTRY_ID2_ACCESSOR);

						String description = article.getDescriptionMapAsXML();

						AssetEntry assetEntry =
							assetEntryLocalService.updateEntry(
								userId, article.getGroupId(),
								article.getCreateDate(),
								article.getModifiedDate(),
								JournalArticle.class.getName(),
								article.getResourcePrimKey(), article.getUuid(),
								getClassTypeId(article), assetCategoryIds,
								assetTagNames, isListable(article), false, null,
								null, null, null, ContentTypes.TEXT_HTML, title,
								description, description, null,
								article.getLayoutUuid(), 0, 0,
								draftAssetEntry.getPriority());

						assetLinkLocalService.updateLinks(
							userId, assetEntry.getEntryId(), assetLinkEntryIds,
							AssetLinkConstants.TYPE_RELATED);

						assetEntryLocalService.deleteEntry(draftAssetEntry);
					}
				}

				assetEntryLocalService.updateEntry(
					JournalArticle.class.getName(),
					article.getResourcePrimKey(), article.getDisplayDate(),
					article.getExpirationDate(), isListable(article), true);

				// Social

				JSONObject extraDataJSONObject = JSONUtil.put("title", title);

				if (serviceContext.isCommandUpdate()) {
					SocialActivityManagerUtil.addActivity(
						user.getUserId(), article,
						JournalActivityKeys.UPDATE_ARTICLE,
						extraDataJSONObject.toString(), 0);
				}
				else {
					SocialActivityManagerUtil.addUniqueActivity(
						user.getUserId(), article,
						JournalActivityKeys.ADD_ARTICLE,
						extraDataJSONObject.toString(), 0);
				}
			}
			else if (oldStatus == WorkflowConstants.STATUS_APPROVED) {
				updatePreviousApprovedArticle(article);
			}
		}

		String action = "update";

		if ((oldStatus != WorkflowConstants.STATUS_IN_TRASH) &&
			(status != WorkflowConstants.STATUS_IN_TRASH)) {

			// Email

			if ((oldStatus == WorkflowConstants.STATUS_PENDING) &&
				((status == WorkflowConstants.STATUS_APPROVED) ||
				 (status == WorkflowConstants.STATUS_DENIED))) {

				String msg = "granted";

				if (status == WorkflowConstants.STATUS_DENIED) {
					msg = "denied";
				}

				try {
					articleURL = buildArticleURL(
						articleURL, article.getGroupId(), article.getFolderId(),
						article.getArticleId());

					sendEmail(article, articleURL, msg, serviceContext);
				}
				catch (Exception e) {
					_log.error(
						StringBundler.concat(
							"Unable to send email to notify the change of ",
							"status to ", msg, " for article ", article.getId(),
							": ", e.getMessage()));
				}
			}

			// Subscriptions

			if (article.equals(
					getOldestArticle(
						article.getGroupId(), article.getArticleId()))) {

				action = "add";
			}
		}

		if ((oldStatus != WorkflowConstants.STATUS_IN_TRASH) &&
			(status == WorkflowConstants.STATUS_IN_TRASH)) {

			action = "move_to_trash";
		}
		else if ((oldStatus == WorkflowConstants.STATUS_IN_TRASH) &&
				 (status != WorkflowConstants.STATUS_IN_TRASH)) {

			action = "move_from_trash";
		}

		notifySubscribers(user.getUserId(), article, action, serviceContext);

		return article;
	}

	/**
	 * Updates the workflow status of the web content article matching the class
	 * PK.
	 *
	 * @param  userId the primary key of the user updating the web content
	 *         article's status
	 * @param  classPK the primary key of the DDM structure, if the web content
	 *         article is related to a DDM structure, the primary key of the
	 *         class associated with the article, or <code>0</code> otherwise
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  workflowContext the web content article's configured workflow
	 * @param  serviceContext the service context to be applied. Can set the
	 *         modification date, portlet preferences, and can set whether to
	 *         add the default command update for the web content article.
	 * @return the updated web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle updateStatus(
			long userId, long classPK, int status,
			Map<String, Serializable> workflowContext,
			ServiceContext serviceContext)
		throws PortalException {

		return journalArticleLocalService.updateStatus(
			userId, getArticle(classPK), status, null, serviceContext,
			workflowContext);
	}

	/**
	 * Updates the workflow status of the web content article matching the
	 * group, article ID, and version.
	 *
	 * @param  userId the primary key of the user updating the web content
	 *         article's status
	 * @param  groupId the primary key of the web content article's group
	 * @param  articleId the primary key of the web content article
	 * @param  version the web content article's version
	 * @param  status the web content article's workflow status. For more
	 *         information see {@link WorkflowConstants} for constants starting
	 *         with the "STATUS_" prefix.
	 * @param  articleURL the web content article's accessible URL
	 * @param  workflowContext the web content article's configured workflow
	 * @param  serviceContext the service context to be applied. Can set the
	 *         modification date, portlet preferences, and can set whether to
	 *         add the default command update for the web content article.
	 * @return the updated web content article
	 * @throws PortalException if a portal exception occurred
	 */
	@Override
	public JournalArticle updateStatus(
			long userId, long groupId, String articleId, double version,
			int status, String articleURL,
			Map<String, Serializable> workflowContext,
			ServiceContext serviceContext)
		throws PortalException {

		JournalArticle article = journalArticlePersistence.findByG_A_V(
			groupId, articleId, version);

		return journalArticleLocalService.updateStatus(
			userId, article, status, articleURL, serviceContext,
			workflowContext);
	}

	/**
	 * Updates the web content articles matching the group, class name ID, and
	 * DDM template key, replacing the DDM template key with a new one.
	 *
	 * @param      groupId the primary key of the web content article's group
	 * @param      classNameId the primary key of the DDMStructure class if the
	 *             web content article is related to a DDM structure, the
	 *             primary key of the class name associated with the article, or
	 *             JournalArticleConstants.CLASSNAME_ID_DEFAULT in the
	 *             journal-api module otherwise
	 * @param      oldDDMTemplateKey the primary key of the web content
	 *             article's old DDM template
	 * @param      newDDMTemplateKey the primary key of the web content
	 *             article's new DDM template
	 * @deprecated As of Wilberforce (7.0.x), replaced by {@link
	 *             #updateDDMTemplateKey}
	 */
	@Deprecated
	@Override
	public void updateTemplateId(
		long groupId, long classNameId, String oldDDMTemplateKey,
		String newDDMTemplateKey) {

		updateDDMTemplateKey(
			groupId, classNameId, oldDDMTemplateKey, newDDMTemplateKey);
	}

	protected void addDocumentLibraryFileEntries(Element dynamicElementElement)
		throws PortalException {

		if (ExportImportThreadLocal.isImportInProcess()) {
			return;
		}

		for (Element dynamicContentElement :
				dynamicElementElement.elements("dynamic-content")) {

			String value = dynamicContentElement.getText();

			if (Validator.isNull(value)) {
				continue;
			}

			JSONObject jsonObject = JSONFactoryUtil.createJSONObject(value);

			boolean tempFile = jsonObject.getBoolean("tempFile");

			if (!tempFile) {
				continue;
			}

			String uuid = jsonObject.getString("uuid");
			long groupId = jsonObject.getLong("groupId");

			FileEntry fileEntry =
				dlAppLocalService.getFileEntryByUuidAndGroupId(uuid, groupId);

			String fileEntryName = DLUtil.getUniqueFileName(
				fileEntry.getGroupId(), fileEntry.getFolderId(),
				fileEntry.getFileName());

			dlAppLocalService.addFileEntry(
				fileEntry.getUserId(), fileEntry.getGroupId(), 0, fileEntryName,
				fileEntry.getMimeType(), fileEntryName, StringPool.BLANK,
				StringPool.BLANK, fileEntry.getContentStream(),
				fileEntry.getSize(), new ServiceContext());
		}
	}

	protected void addImageFileEntries(
			JournalArticle article, Element dynamicElementElement)
		throws PortalException {

		if (ExportImportThreadLocal.isImportInProcess()) {
			return;
		}

		Set<Long> tempFileEntryIds = new HashSet<>();

		try {
			for (Element dynamicContentElement :
					dynamicElementElement.elements("dynamic-content")) {

				String value = dynamicContentElement.getText();

				if (Validator.isNull(value)) {
					continue;
				}

				JSONObject jsonObject = JSONFactoryUtil.createJSONObject(value);

				String uuid = jsonObject.getString("uuid");
				long groupId = jsonObject.getLong("groupId");

				FileEntry fileEntry =
					dlAppLocalService.getFileEntryByUuidAndGroupId(
						uuid, groupId);

				boolean tempFile = fileEntry.isRepositoryCapabilityProvided(
					TemporaryFileEntriesCapability.class);

				if (tempFile) {
					FileEntry tempFileEntry = fileEntry;

					Folder folder = article.addImagesFolder();

					String fileEntryName = DLUtil.getUniqueFileName(
						folder.getGroupId(), folder.getFolderId(),
						fileEntry.getFileName());

					fileEntry = PortletFileRepositoryUtil.addPortletFileEntry(
						folder.getGroupId(), fileEntry.getUserId(),
						JournalArticle.class.getName(),
						article.getResourcePrimKey(),
						JournalConstants.SERVICE_NAME, folder.getFolderId(),
						fileEntry.getContentStream(), fileEntryName,
						fileEntry.getMimeType(), false);

					tempFileEntryIds.add(tempFileEntry.getFileEntryId());
				}

				JSONObject cdataJSONObject = JSONFactoryUtil.createJSONObject(
					dynamicContentElement.getText());

				cdataJSONObject.put(
					"fileEntryId", fileEntry.getFileEntryId()
				).put(
					"resourcePrimKey", article.getResourcePrimKey()
				).put(
					"uuid", fileEntry.getUuid()
				);

				dynamicContentElement.clearContent();

				dynamicContentElement.addCDATA(cdataJSONObject.toString());
			}
		}
		finally {
			for (Long tempFileEntryId : tempFileEntryIds) {
				TempFileEntryUtil.deleteTempFileEntry(tempFileEntryId);
			}
		}
	}

	protected String buildArticleURL(
		String articleURL, long groupId, long folderId, String articleId) {

		String portletId = PortletProviderUtil.getPortletId(
			JournalArticle.class.getName(), PortletProvider.Action.EDIT);

		String namespace = _portal.getPortletNamespace(portletId);

		articleURL = _http.addParameter(
			articleURL, namespace + "groupId", groupId);
		articleURL = _http.addParameter(
			articleURL, namespace + "folderId", folderId);
		articleURL = _http.addParameter(
			articleURL, namespace + "articleId", articleId);

		return articleURL;
	}

	protected SearchContext buildSearchContext(
		long companyId, long groupId, List<Long> folderIds, long classNameId,
		String articleId, String title, String description, String content,
		int status, String ddmStructureKey, String ddmTemplateKey,
		LinkedHashMap<String, Object> params, boolean andSearch, int start,
		int end, Sort sort) {

		SearchContext searchContext = new SearchContext();

		searchContext.setAndSearch(andSearch);

		Map<String, Serializable> attributes = new HashMap<>();

		attributes.put(Field.ARTICLE_ID, articleId);
		attributes.put(Field.CLASS_NAME_ID, classNameId);
		attributes.put(Field.CONTENT, content);
		attributes.put(Field.DESCRIPTION, description);
		attributes.put(Field.STATUS, status);
		attributes.put(Field.TITLE, title);
		attributes.put("ddmStructureKey", ddmStructureKey);
		attributes.put("ddmTemplateKey", ddmTemplateKey);
		attributes.put("params", params);

		searchContext.setAttributes(attributes);

		searchContext.setCompanyId(companyId);
		searchContext.setEnd(end);
		searchContext.setFolderIds(folderIds);
		searchContext.setGroupIds(new long[] {groupId});

		if (params != null) {
			searchContext.setIncludeDiscussions(
				GetterUtil.getBoolean(params.get("includeDiscussions")));

			String keywords = (String)params.remove("keywords");

			if (Validator.isNotNull(keywords)) {
				searchContext.setKeywords(keywords);
			}
		}

		QueryConfig queryConfig = searchContext.getQueryConfig();

		queryConfig.setHighlightEnabled(false);
		queryConfig.setScoreEnabled(false);

		if (sort != null) {
			searchContext.setSorts(sort);
		}

		searchContext.setStart(start);

		return searchContext;
	}

	protected SearchContext buildSearchContext(
			long groupId, long userId, long creatorUserId, int status,
			int start, int end)
		throws PortalException {

		SearchContext searchContext = new SearchContext();

		searchContext.setAttribute(Field.STATUS, status);

		searchContext.setAttribute("paginationType", "none");

		if (creatorUserId > 0) {
			searchContext.setAttribute(
				Field.USER_ID, String.valueOf(creatorUserId));
		}

		Group group = groupLocalService.getGroup(groupId);

		searchContext.setCompanyId(group.getCompanyId());

		searchContext.setEnd(end);
		searchContext.setGroupIds(new long[] {groupId});
		searchContext.setSorts(new Sort(Field.MODIFIED_DATE, true));
		searchContext.setStart(start);
		searchContext.setUserId(userId);

		return searchContext;
	}

	protected void checkArticlesByCompanyIdAndExpirationDate(
			long companyId, Date expirationDate, Date nextExpirationDate)
		throws PortalException {

		IndexableActionableDynamicQuery indexableActionableDynamicQuery =
			getIndexableActionableDynamicQuery();

		Indexer<JournalArticle> indexer = IndexerRegistryUtil.getIndexer(
			JournalArticle.class);

		indexableActionableDynamicQuery.setAddCriteriaMethod(
			dynamicQuery -> {
				Property classNameIdProperty = PropertyFactoryUtil.forName(
					"classNameId");

				dynamicQuery.add(
					classNameIdProperty.eq(
						JournalArticleConstants.CLASSNAME_ID_DEFAULT));

				Property expirationDateProperty = PropertyFactoryUtil.forName(
					"expirationDate");

				dynamicQuery.add(expirationDateProperty.le(nextExpirationDate));

				Property statusProperty = PropertyFactoryUtil.forName("status");

				dynamicQuery.add(
					statusProperty.eq(WorkflowConstants.STATUS_APPROVED));
			});
		indexableActionableDynamicQuery.setCompanyId(companyId);
		indexableActionableDynamicQuery.setPerformActionMethod(
			(JournalArticle article) -> {
				if (_log.isDebugEnabled()) {
					_log.debug("Expiring article " + article.getId());
				}

				if (isExpireAllArticleVersions(companyId)) {
					List<JournalArticle> currentArticles =
						journalArticlePersistence.findByG_A(
							article.getGroupId(), article.getArticleId(),
							QueryUtil.ALL_POS, QueryUtil.ALL_POS,
							new ArticleVersionComparator(true));

					for (JournalArticle currentArticle : currentArticles) {
						if ((currentArticle.getExpirationDate() == null) ||
							(currentArticle.getVersion() >
								article.getVersion())) {

							continue;
						}

						currentArticle.setExpirationDate(
							article.getExpirationDate());
						currentArticle.setStatus(
							WorkflowConstants.STATUS_EXPIRED);

						journalArticlePersistence.update(currentArticle);
					}
				}

				article.setStatus(WorkflowConstants.STATUS_EXPIRED);

				journalArticlePersistence.update(article);

				updatePreviousApprovedArticle(article);

				if (indexer != null) {
					indexableActionableDynamicQuery.addDocuments(
						indexer.getDocument(article));
				}
			});

		if (indexer != null) {
			indexableActionableDynamicQuery.setSearchEngineId(
				indexer.getSearchEngineId());
		}

		indexableActionableDynamicQuery.setTransactionConfig(
			DefaultActionableDynamicQuery.REQUIRES_NEW_TRANSACTION_CONFIG);

		indexableActionableDynamicQuery.performActions();
	}

	protected void checkArticlesByDisplayDate(Date displayDate)
		throws PortalException {

		if (_log.isDebugEnabled()) {
			_log.debug(
				StringBundler.concat(
					"Publishing articles with display date less than ",
					displayDate, " and status ",
					WorkflowConstants.STATUS_SCHEDULED));
		}

		ActionableDynamicQuery actionableDynamicQuery =
			getActionableDynamicQuery();

		actionableDynamicQuery.setAddCriteriaMethod(
			dynamicQuery -> {
				Property displayDateProperty = PropertyFactoryUtil.forName(
					"displayDate");

				dynamicQuery.add(displayDateProperty.lt(displayDate));

				Property statusProperty = PropertyFactoryUtil.forName("status");

				dynamicQuery.add(
					statusProperty.eq(WorkflowConstants.STATUS_SCHEDULED));
			});
		actionableDynamicQuery.setPerformActionMethod(
			(JournalArticle article) -> {
				if (_log.isDebugEnabled()) {
					_log.debug("Publishing article " + article.getId());
				}

				long userId = _portal.getValidUserId(
					article.getCompanyId(), article.getUserId());

				ServiceContext serviceContext = new ServiceContext();

				serviceContext.setCommand(Constants.UPDATE);

				String portletId = PortletProviderUtil.getPortletId(
					JournalArticle.class.getName(),
					PortletProvider.Action.EDIT);

				String layoutFullURL = _portal.getLayoutFullURL(
					article.getGroupId(), portletId);

				serviceContext.setLayoutFullURL(layoutFullURL);

				serviceContext.setScopeGroupId(article.getGroupId());

				journalArticleLocalService.updateStatus(
					userId, article, WorkflowConstants.STATUS_APPROVED, null,
					serviceContext, new HashMap<>());
			});
		actionableDynamicQuery.setTransactionConfig(
			DefaultActionableDynamicQuery.REQUIRES_NEW_TRANSACTION_CONFIG);

		actionableDynamicQuery.performActions();
	}

	protected void checkArticlesByExpirationDate(Date expirationDate)
		throws PortalException {

		long checkInterval = getArticleCheckInterval();

		Date nextExpirationDate = new Date(
			expirationDate.getTime() + checkInterval);

		if (_log.isDebugEnabled()) {
			_log.debug(
				StringBundler.concat(
					"Expiring articles with expiration date less than or ",
					"equal to ", nextExpirationDate, " and status ",
					WorkflowConstants.STATUS_APPROVED));
		}

		for (Company company : companyLocalService.getCompanies(false)) {
			checkArticlesByCompanyIdAndExpirationDate(
				company.getCompanyId(), expirationDate, nextExpirationDate);
		}

		if (_previousCheckDate == null) {
			_previousCheckDate = new Date(
				expirationDate.getTime() - getArticleCheckInterval());
		}
	}

	protected void checkArticlesByReviewDate(Date reviewDate)
		throws PortalException {

		if (_log.isDebugEnabled()) {
			_log.debug(
				StringBundler.concat(
					"Sending review notification for articles with reviewDate ",
					"between ", _previousCheckDate, " and ", reviewDate));
		}

		Set<Long> latestArticleIds = new HashSet<>();

		List<JournalArticle> articles = journalArticleFinder.findByReviewDate(
			JournalArticleConstants.CLASSNAME_ID_DEFAULT, reviewDate,
			_previousCheckDate);

		for (JournalArticle article : articles) {
			if (article.isInTrash()) {
				continue;
			}

			long groupId = article.getGroupId();
			String articleId = article.getArticleId();

			if (!journalArticleLocalService.isLatestVersion(
					groupId, articleId, article.getVersion())) {

				article = journalArticleLocalService.getLatestArticle(
					groupId, articleId);
			}

			if (latestArticleIds.add(article.getPrimaryKey())) {
				if (_log.isDebugEnabled()) {
					_log.debug(
						"Sending review notification for article " +
							article.getId());
				}

				String portletId = PortletProviderUtil.getPortletId(
					JournalArticle.class.getName(),
					PortletProvider.Action.EDIT);

				String articleURL = _portal.getControlPanelFullURL(
					article.getGroupId(), portletId, null);

				articleURL = buildArticleURL(
					articleURL, article.getGroupId(), article.getFolderId(),
					article.getArticleId());

				sendEmail(article, articleURL, "review", new ServiceContext());
			}
		}
	}

	protected void checkStructure(Document contentDocument, DDMForm ddmForm)
		throws PortalException {

		for (DDMFormField ddmFormField : ddmForm.getDDMFormFields()) {
			checkStructureField(ddmFormField, contentDocument.getRootElement());
		}
	}

	protected void checkStructure(JournalArticle article)
		throws PortalException {

		checkStructure(article, article.getDDMStructure());
	}

	protected void checkStructure(
			JournalArticle article, DDMStructure ddmStructure)
		throws PortalException {

		checkStructure(article.getDocument(), ddmStructure.getDDMForm());
	}

	protected void checkStructureField(
			DDMFormField ddmFormField, Element contentElement)
		throws PortalException {

		String fieldName = ddmFormField.getName();

		boolean hasField = false;

		for (Element childElement : contentElement.elements()) {
			if (fieldName.equals(
					childElement.attributeValue("name", StringPool.BLANK))) {

				hasField = true;

				for (DDMFormField childDDMFormField :
						ddmFormField.getNestedDDMFormFields()) {

					checkStructureField(childDDMFormField, childElement);
				}

				break;
			}
		}

		if (!hasField) {
			String contentElementType = contentElement.attributeValue(
				"type", StringPool.BLANK);

			if (!contentElementType.equals("list") &&
				!contentElementType.equals("multi-list")) {

				throw new StructureDefinitionException(fieldName);
			}
		}
	}

	protected void copyArticleImages(
			JournalArticle oldArticle, JournalArticle newArticle)
		throws Exception {

		Folder folder = newArticle.addImagesFolder();

		for (FileEntry fileEntry : oldArticle.getImagesFileEntries()) {
			PortletFileRepositoryUtil.addPortletFileEntry(
				oldArticle.getGroupId(), newArticle.getUserId(),
				JournalArticle.class.getName(), newArticle.getResourcePrimKey(),
				JournalConstants.SERVICE_NAME, folder.getFolderId(),
				fileEntry.getContentStream(), fileEntry.getFileName(),
				fileEntry.getMimeType(), false);
		}

		Document contentDocument = oldArticle.getDocument();

		contentDocument = contentDocument.clone();

		XPath xPathSelector = SAXReaderUtil.createXPath(
			"//dynamic-element[@type='image']");

		List<Node> imageNodes = xPathSelector.selectNodes(contentDocument);

		for (Node imageNode : imageNodes) {
			Element imageEl = (Element)imageNode;

			List<Element> dynamicContentEls = imageEl.elements(
				"dynamic-content");

			for (Element dynamicContentEl : dynamicContentEls) {
				String fileName = dynamicContentEl.attributeValue("name");

				FileEntry fileEntry =
					PortletFileRepositoryUtil.getPortletFileEntry(
						newArticle.getGroupId(), folder.getFolderId(),
						fileName);

				String previewURL = _dlURLHelper.getPreviewURL(
					fileEntry, fileEntry.getFileVersion(), null,
					StringPool.BLANK, false, true);

				dynamicContentEl.addAttribute(
					"resourcePrimKey",
					String.valueOf(newArticle.getResourcePrimKey()));

				dynamicContentEl.clearContent();

				dynamicContentEl.addCDATA(previewURL);
			}
		}

		newArticle.setContent(contentDocument.formattedString());
	}

	protected Map<String, LocalizedValue> createFieldsValuesMap(
		Element parentElement, Locale defaultLocale) {

		Map<String, LocalizedValue> fieldsValuesMap = new HashMap<>();

		List<Element> dynamicElementElements = parentElement.elements(
			"dynamic-element");

		for (Element dynamicElementElement : dynamicElementElements) {
			String fieldName = dynamicElementElement.attributeValue(
				"name", StringPool.BLANK);
			LocalizedValue fieldLocalizedValue = new LocalizedValue(
				defaultLocale);

			List<Element> dynamicContentElements =
				dynamicElementElement.elements("dynamic-content");

			for (Element dynamicContentElement : dynamicContentElements) {
				String languageId = dynamicContentElement.attributeValue(
					"language-id");
				String value = dynamicContentElement.getText();

				fieldLocalizedValue.addString(
					LocaleUtil.fromLanguageId(languageId), value);
			}

			fieldsValuesMap.put(fieldName, fieldLocalizedValue);

			fieldsValuesMap.putAll(
				createFieldsValuesMap(dynamicElementElement, defaultLocale));
		}

		return fieldsValuesMap;
	}

	protected Map<String, LocalizedValue> createFieldsValuesMap(
		String content) {

		try {
			Document document = SAXReaderUtil.read(content);

			Element rootElement = document.getRootElement();

			Locale defaultLocale = LocaleUtil.fromLanguageId(
				rootElement.attributeValue("default-locale"));

			return createFieldsValuesMap(rootElement, defaultLocale);
		}
		catch (DocumentException de) {
			throw new SystemException(de);
		}
	}

	protected JournalArticle fetchLatestLiveArticle(JournalArticle article)
		throws PortalException {

		Group group = groupLocalService.getGroup(article.getGroupId());

		long liveGroupId = group.getLiveGroupId();

		if (liveGroupId == 0) {
			return null;
		}

		JournalArticleResource articleResource =
			_journalArticleResourceLocalService.
				fetchJournalArticleResourceByUuidAndGroupId(
					article.getArticleResourceUuid(), liveGroupId);

		if (articleResource == null) {
			return null;
		}

		return journalArticleLocalService.fetchLatestArticle(
			articleResource.getResourcePrimKey(), WorkflowConstants.STATUS_ANY,
			false);
	}

	protected void format(
			User user, long groupId, JournalArticle article, Element root)
		throws PortalException {

		for (Element element : root.elements()) {
			String elType = element.attributeValue("type", StringPool.BLANK);

			if (elType.equals("document_library")) {
				addDocumentLibraryFileEntries(element);
			}
			else if (elType.equals("image")) {
				addImageFileEntries(article, element);
			}
			else if (elType.equals("text_area") || elType.equals("text") ||
					 elType.equals("text_box")) {

				List<Element> dynamicContentElements = element.elements(
					"dynamic-content");

				for (Element dynamicContentElement : dynamicContentElements) {
					String dynamicContent = dynamicContentElement.getText();

					if (Validator.isNotNull(dynamicContent)) {
						String contentType = ContentTypes.TEXT_PLAIN;

						if (elType.equals("text_area")) {
							contentType = ContentTypes.TEXT_HTML;
						}

						dynamicContent = SanitizerUtil.sanitize(
							user.getCompanyId(), groupId, user.getUserId(),
							JournalArticle.class.getName(), 0, contentType,
							dynamicContent);

						dynamicContentElement.clearContent();

						dynamicContentElement.addCDATA(dynamicContent);
					}
				}
			}

			format(user, groupId, article, element);
		}
	}

	protected String format(
			User user, long groupId, JournalArticle article, String content)
		throws PortalException {

		Document document = null;

		try {
			document = SAXReaderUtil.read(content);

			format(user, groupId, article, document.getRootElement());

			content = XMLUtil.formatXML(document);
		}
		catch (DocumentException de) {
			_log.error(de, de);
		}

		return content;
	}

	protected long getArticleCheckInterval() {
		try {
			JournalServiceConfiguration journalServiceConfiguration =
				configurationProvider.getCompanyConfiguration(
					JournalServiceConfiguration.class,
					CompanyThreadLocal.getCompanyId());

			return journalServiceConfiguration.checkInterval() * Time.MINUTE;
		}
		catch (PortalException pe) {
			throw new RuntimeException(pe);
		}
	}

	protected Locale getArticleDefaultLocale(String content) {
		String defaultLanguageId = LocalizationUtil.getDefaultLanguageId(
			content);

		if (Validator.isNotNull(defaultLanguageId)) {
			return LocaleUtil.fromLanguageId(defaultLanguageId);
		}

		return LocaleUtil.getSiteDefault();
	}

	protected JournalArticleDisplay getArticleDisplay(
			JournalArticle article, String ddmTemplateKey, String viewMode,
			String languageId, int page,
			PortletRequestModel portletRequestModel, ThemeDisplay themeDisplay,
			boolean propagateException)
		throws PortalException {

		String content = null;

		if (page < 1) {
			page = 1;
		}

		int numberOfPages = 1;
		boolean paginate = false;
		boolean pageFlow = false;

		boolean cacheable = true;

		Map<String, String> tokens = JournalUtil.getTokens(
			article.getGroupId(), portletRequestModel, themeDisplay);

		if ((themeDisplay == null) && (portletRequestModel == null)) {
			tokens.put("company_id", String.valueOf(article.getCompanyId()));

			Group companyGroup = groupLocalService.getCompanyGroup(
				article.getCompanyId());

			tokens.put(
				"article_group_id", String.valueOf(article.getGroupId()));
			tokens.put(
				"company_group_id", String.valueOf(companyGroup.getGroupId()));

			// Deprecated tokens

			tokens.put("group_id", String.valueOf(article.getGroupId()));
		}

		tokens.put(
			TemplateConstants.CLASS_NAME_ID,
			String.valueOf(
				classNameLocalService.getClassNameId(DDMStructure.class)));
		tokens.put(
			"article_resource_pk",
			String.valueOf(article.getResourcePrimKey()));

		DDMStructure ddmStructure = article.getDDMStructure();

		tokens.put(
			"ddm_structure_key",
			String.valueOf(ddmStructure.getStructureKey()));
		tokens.put(
			"ddm_structure_id", String.valueOf(ddmStructure.getStructureId()));

		// Deprecated token

		tokens.put("structure_id", article.getDDMStructureKey());

		Document document = article.getDocument();

		document = document.clone();

		Element rootElement = document.getRootElement();

		List<Element> pages = rootElement.elements("page");

		if (!pages.isEmpty()) {
			pageFlow = true;

			String targetPage = null;

			Map<String, String[]> parameters =
				portletRequestModel.getParameters();

			if (parameters != null) {
				String[] values = parameters.get("targetPage");

				if ((values != null) && (values.length > 0)) {
					targetPage = values[0];
				}
			}

			Element pageElement = null;

			if (Validator.isNotNull(targetPage)) {
				targetPage = HtmlUtil.escapeXPathAttribute(targetPage);

				XPath xPathSelector = SAXReaderUtil.createXPath(
					"/root/page[@id = " + targetPage + "]");

				pageElement = (Element)xPathSelector.selectSingleNode(document);
			}

			if (pageElement != null) {
				document = SAXReaderUtil.createDocument(pageElement);

				rootElement = document.getRootElement();

				numberOfPages = pages.size();
			}
			else {
				if (page > pages.size()) {
					page = 1;
				}

				pageElement = pages.get(page - 1);

				document = SAXReaderUtil.createDocument(pageElement);

				rootElement = document.getRootElement();

				numberOfPages = pages.size();
				paginate = true;
			}
		}

		JournalUtil.addAllReservedEls(
			rootElement, tokens, article, languageId, themeDisplay);

		try {
			if (_log.isDebugEnabled()) {
				_log.debug(
					StringBundler.concat(
						"Transforming ", article.getArticleId(),
						StringPool.SPACE, article.getVersion(),
						StringPool.SPACE, languageId));
			}

			// Try with specified template first (in the current group and the
			// global group). If a template is not specified, use the default
			// one. If the specified template does not exist, use the default
			// one. If the default one does not exist, throw an exception.

			DDMTemplate ddmTemplate = ddmTemplateLocalService.fetchTemplate(
				_portal.getSiteGroupId(article.getGroupId()),
				classNameLocalService.getClassNameId(DDMStructure.class),
				ddmTemplateKey, true);

			if ((ddmTemplate == null) &&
				!Objects.equals(article.getDDMTemplateKey(), ddmTemplateKey)) {

				ddmTemplate = ddmTemplateLocalService.fetchTemplate(
					_portal.getSiteGroupId(article.getGroupId()),
					classNameLocalService.getClassNameId(DDMStructure.class),
					article.getDDMTemplateKey(), true);
			}

			if (ddmTemplate != null) {
				Group companyGroup = groupLocalService.getCompanyGroup(
					article.getCompanyId());

				if (companyGroup.getGroupId() == ddmTemplate.getGroupId()) {
					tokens.put(
						"company_group_id",
						String.valueOf(companyGroup.getGroupId()));
				}
			}

			String script = StringPool.BLANK;
			String langType = StringPool.BLANK;

			if (ddmTemplate != null) {
				tokens.put(
					"ddm_template_key",
					String.valueOf(ddmTemplate.getTemplateKey()));
				tokens.put(
					"ddm_template_id",
					String.valueOf(ddmTemplate.getTemplateId()));

				// Deprecated token

				tokens.put("template_id", ddmTemplateKey);

				script = ddmTemplate.getScript();
				langType = ddmTemplate.getLanguage();
				cacheable = ddmTemplate.isCacheable();
			}
			else {
				script = _journalDefaultTemplateProvider.getScript(
					ddmStructure.getStructureId());

				langType = _journalDefaultTemplateProvider.getLanguage();
				cacheable = _journalDefaultTemplateProvider.isCacheable();
			}

			content = JournalUtil.transform(
				themeDisplay, tokens, viewMode, languageId, document,
				portletRequestModel, script, langType, propagateException);

			if (!pageFlow) {
				JournalServiceConfiguration journalServiceConfiguration =
					configurationProvider.getCompanyConfiguration(
						JournalServiceConfiguration.class,
						article.getCompanyId());

				String[] pieces = StringUtil.split(
					content,
					journalServiceConfiguration.journalArticlePageBreakToken());

				if (pieces.length > 1) {
					if (page > pieces.length) {
						page = 1;
					}

					content = pieces[page - 1];
					numberOfPages = pieces.length;
					paginate = true;
				}
			}
		}
		catch (Exception e) {
			throw new SystemException(e);
		}

		return new JournalArticleDisplayImpl(
			article.getCompanyId(), article.getId(),
			article.getResourcePrimKey(), article.getGroupId(),
			article.getUserId(), article.getArticleId(), article.getVersion(),
			article.getTitle(languageId), article.getUrlTitle(),
			article.getDescription(languageId),
			article.getAvailableLanguageIds(), content,
			article.getDDMStructureKey(), ddmTemplateKey,
			article.isSmallImage(), article.getSmallImageId(),
			article.getSmallImageURL(), numberOfPages, page, paginate,
			cacheable);
	}

	protected List<ObjectValuePair<Long, Integer>> getArticleVersionStatuses(
		List<JournalArticle> articles) {

		List<ObjectValuePair<Long, Integer>> articleVersionStatusOVPs =
			new ArrayList<>(articles.size());

		for (JournalArticle article : articles) {
			int status = article.getStatus();

			if (status == WorkflowConstants.STATUS_PENDING) {
				status = WorkflowConstants.STATUS_DRAFT;
			}

			ObjectValuePair<Long, Integer> articleVersionStatusOVP =
				new ObjectValuePair<>(article.getId(), status);

			articleVersionStatusOVPs.add(articleVersionStatusOVP);
		}

		return articleVersionStatusOVPs;
	}

	protected long getClassTypeId(JournalArticle article) {
		long classNameId = classNameLocalService.getClassNameId(
			JournalArticle.class);

		DDMStructure ddmStructure = ddmStructureLocalService.fetchStructure(
			_portal.getSiteGroupId(article.getGroupId()), classNameId,
			article.getDDMStructureKey(), true);

		return ddmStructure.getStructureId();
	}

	protected JournalArticle getFirstArticle(
			long groupId, String articleId, int status,
			OrderByComparator<JournalArticle> orderByComparator)
		throws PortalException {

		if (status == WorkflowConstants.STATUS_ANY) {
			return journalArticlePersistence.findByG_A_NotST_First(
				groupId, articleId, WorkflowConstants.STATUS_IN_TRASH,
				orderByComparator);
		}

		return journalArticlePersistence.findByG_A_ST_First(
			groupId, articleId, status, orderByComparator);
	}

	protected JournalGroupServiceConfiguration
			getJournalGroupServiceConfiguration(long groupId)
		throws ConfigurationException {

		return configurationProvider.getConfiguration(
			JournalGroupServiceConfiguration.class,
			new GroupServiceSettingsLocator(
				groupId, JournalConstants.SERVICE_NAME));
	}

	protected double getNextVersion(JournalArticle article)
		throws PortalException {

		double nextVersion = article.getVersion();

		// The next version must be greater than the version of the latest live
		// article

		JournalArticle latestLiveArticle = fetchLatestLiveArticle(article);

		if ((latestLiveArticle != null) &&
			(latestLiveArticle.getVersion() > nextVersion)) {

			nextVersion = latestLiveArticle.getVersion();
		}

		return MathUtil.format(nextVersion + 0.1, 1, 1);
	}

	protected String getUniqueUrlTitle(
			long id, long groupId, String articleId, String title)
		throws PortalException {

		String urlTitle = JournalUtil.getUrlTitle(id, title);

		return getUniqueUrlTitle(groupId, articleId, urlTitle);
	}

	protected String getUniqueUrlTitle(
			long id, long groupId, String articleId, String title,
			String oldUrlTitle, ServiceContext serviceContext)
		throws PortalException {

		String serviceContextUrlTitle = ParamUtil.getString(
			serviceContext, "urlTitle");

		String urlTitle = null;

		if (Validator.isNotNull(serviceContextUrlTitle)) {
			urlTitle = JournalUtil.getUrlTitle(id, serviceContextUrlTitle);
		}
		else if (Validator.isNotNull(oldUrlTitle)) {
			return oldUrlTitle;
		}
		else {
			urlTitle = getUniqueUrlTitle(id, groupId, articleId, title);
		}

		JournalArticle urlTitleArticle = fetchArticleByUrlTitle(
			groupId, urlTitle);

		if ((urlTitleArticle != null) &&
			!Objects.equals(urlTitleArticle.getArticleId(), articleId)) {

			urlTitle = getUniqueUrlTitle(id, groupId, articleId, urlTitle);
		}

		return urlTitle;
	}

	protected String getURLViewInContext(
		JournalArticle article, String portletId,
		ServiceContext serviceContext) {

		LiferayPortletRequest liferayPortletRequest =
			serviceContext.getLiferayPortletRequest();

		if (liferayPortletRequest == null) {
			return StringPool.BLANK;
		}

		String urlViewInContext = StringPool.BLANK;

		try {
			String defaultArticleURL = _portal.getControlPanelFullURL(
				article.getGroupId(), portletId, null);

			AssetRendererFactory<JournalArticle> assetRendererFactory =
				AssetRendererFactoryRegistryUtil.getAssetRendererFactoryByClass(
					JournalArticle.class);

			AssetRenderer<JournalArticle> assetRenderer =
				assetRendererFactory.getAssetRenderer(
					article, AssetRendererFactory.TYPE_LATEST_APPROVED);

			urlViewInContext = assetRenderer.getURLViewInContext(
				liferayPortletRequest, null, defaultArticleURL);
		}
		catch (Exception e) {
			_log.error(e, e);
		}

		return urlViewInContext;
	}

	protected boolean hasModifiedLatestApprovedVersion(
		long groupId, String articleId, double version) {

		JournalArticle article = fetchLatestArticle(
			groupId, articleId, WorkflowConstants.STATUS_APPROVED);

		if ((article == null) || (article.getVersion() <= version)) {
			return true;
		}

		return false;
	}

	protected boolean isArticleCommentsEnabled(long companyId)
		throws PortalException {

		JournalServiceConfiguration journalServiceConfiguration =
			configurationProvider.getCompanyConfiguration(
				JournalServiceConfiguration.class, companyId);

		return journalServiceConfiguration.articleCommentsEnabled();
	}

	protected boolean isExpireAllArticleVersions(long companyId)
		throws PortalException {

		JournalServiceConfiguration journalServiceConfiguration =
			configurationProvider.getCompanyConfiguration(
				JournalServiceConfiguration.class, companyId);

		return journalServiceConfiguration.expireAllArticleVersionsEnabled();
	}

	protected boolean isReindexAllArticleVersions() {
		try {
			JournalServiceConfiguration journalServiceConfiguration =
				configurationProvider.getCompanyConfiguration(
					JournalServiceConfiguration.class,
					CompanyThreadLocal.getCompanyId());

			return journalServiceConfiguration.indexAllArticleVersionsEnabled();
		}
		catch (Exception e) {
			_log.error(e, e);
		}

		return false;
	}

	protected void notifySubscribers(
			long userId, JournalArticle article, String action,
			ServiceContext serviceContext)
		throws PortalException {

		JournalGroupServiceConfiguration journalGroupServiceConfiguration =
			getJournalGroupServiceConfiguration(article.getGroupId());

		if (action.equals("add") &&
			journalGroupServiceConfiguration.emailArticleAddedEnabled()) {
		}
		else if (action.equals("move_to") &&
				 journalGroupServiceConfiguration.
					 emailArticleMovedToFolderEnabled()) {
		}
		else if (action.equals("move_to_trash") &&
				 journalGroupServiceConfiguration.
					 emailArticleMovedToTrashEnabled()) {
		}
		else if (action.equals("move_from") &&
				 journalGroupServiceConfiguration.
					 emailArticleMovedFromFolderEnabled()) {
		}
		else if (action.equals("move_from_trash") &&
				 journalGroupServiceConfiguration.
					 emailArticleMovedToTrashEnabled()) {
		}
		else if (action.equals("update") &&
				 journalGroupServiceConfiguration.
					 emailArticleUpdatedEnabled()) {
		}
		else {
			return;
		}

		String articleTitle = article.getTitle(serviceContext.getLanguageId());

		String fromName = journalGroupServiceConfiguration.emailFromName();
		String fromAddress =
			journalGroupServiceConfiguration.emailFromAddress();

		Map<Locale, String> localizedSubjectMap = null;
		Map<Locale, String> localizedBodyMap = null;

		int notificationType =
			UserNotificationDefinition.NOTIFICATION_TYPE_ADD_ENTRY;

		if (action.equals("add")) {
			localizedSubjectMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.emailArticleAddedSubject());
			localizedBodyMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.emailArticleAddedBody());
		}
		else if (action.equals("move_to")) {
			localizedSubjectMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.
					emailArticleMovedToFolderSubject());
			localizedBodyMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.
					emailArticleMovedToFolderBody());

			notificationType =
				JournalArticleConstants.NOTIFICATION_TYPE_MOVE_ENTRY_TO_FOLDER;
		}
		else if (action.equals("move_to_trash")) {
			localizedSubjectMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.
					emailArticleMovedToTrashSubject());
			localizedBodyMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.
					emailArticleMovedToTrashBody());

			notificationType =
				JournalArticleConstants.NOTIFICATION_TYPE_MOVE_ENTRY_TO_TRASH;
		}
		else if (action.equals("move_from")) {
			localizedSubjectMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.
					emailArticleMovedFromFolderSubject());
			localizedBodyMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.
					emailArticleMovedFromFolderBody());

			notificationType =
				JournalArticleConstants.
					NOTIFICATION_TYPE_MOVE_ENTRY_FROM_FOLDER;
		}
		else if (action.equals("move_from_trash")) {
			localizedSubjectMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.
					emailArticleMovedFromTrashSubject());
			localizedBodyMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.
					emailArticleMovedFromTrashBody());

			notificationType =
				JournalArticleConstants.NOTIFICATION_TYPE_MOVE_ENTRY_FROM_TRASH;
		}
		else if (action.equals("update")) {
			localizedSubjectMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.emailArticleUpdatedSubject());
			localizedBodyMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.emailArticleUpdatedBody());

			notificationType =
				UserNotificationDefinition.NOTIFICATION_TYPE_UPDATE_ENTRY;
		}

		String articleContent = StringPool.BLANK;
		String articleDiffs = StringPool.BLANK;

		JournalArticle previousApprovedArticle = getPreviousApprovedArticle(
			article);

		try {
			PortletRequestModel portletRequestModel = null;

			if (!ExportImportThreadLocal.isImportInProcess()) {
				portletRequestModel = new PortletRequestModel(
					serviceContext.getLiferayPortletRequest(),
					serviceContext.getLiferayPortletResponse());
			}

			JournalArticleDisplay articleDisplay = getArticleDisplay(
				article, article.getDDMTemplateKey(), Constants.VIEW,
				LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault()), 1,
				portletRequestModel, serviceContext.getThemeDisplay());

			articleContent = articleDisplay.getContent();

			articleDiffs = _journalHelper.diffHtml(
				article.getGroupId(), article.getArticleId(),
				previousApprovedArticle.getVersion(), article.getVersion(),
				LocaleUtil.toLanguageId(LocaleUtil.getSiteDefault()),
				portletRequestModel, serviceContext.getThemeDisplay());
		}
		catch (Exception e) {
		}

		SubscriptionSender subscriptionSender =
			new GroupSubscriptionCheckSubscriptionSender(
				JournalConstants.RESOURCE_NAME);

		subscriptionSender.setClassName(article.getModelClassName());
		subscriptionSender.setClassPK(article.getId());
		subscriptionSender.setCompanyId(article.getCompanyId());
		subscriptionSender.setContextAttribute(
			"[$ARTICLE_CONTENT$]", articleContent, false);
		subscriptionSender.setContextAttribute(
			"[$ARTICLE_DIFFS$]", DiffHtmlUtil.replaceStyles(articleDiffs),
			false);

		String folderName = StringPool.BLANK;

		JournalFolder folder = journalFolderPersistence.fetchByPrimaryKey(
			article.getFolderId());

		if (folder != null) {
			folderName = folder.getName();

			if ((folder.getFolderId() ==
					JournalFolderConstants.DEFAULT_PARENT_FOLDER_ID) &&
				Validator.isNull(folderName)) {

				folderName = LanguageUtil.get(
					LocaleUtil.getSiteDefault(), "home");
			}
		}

		String portletId = PortletProviderUtil.getPortletId(
			JournalArticle.class.getName(), PortletProvider.Action.EDIT);

		String articleURL = getURLViewInContext(
			article, portletId, serviceContext);

		String articleStatus = LanguageUtil.get(
			LocaleUtil.getSiteDefault(),
			WorkflowConstants.getStatusLabel(article.getStatus()));

		subscriptionSender.setContextAttributes(
			"[$ARTICLE_ID$]", article.getArticleId(), "[$ARTICLE_TITLE$]",
			articleTitle, "[$ARTICLE_URL$]", articleURL, "[$ARTICLE_VERSION$]",
			article.getVersion(), "[$FOLDER_NAME$]", folderName,
			"[$ARTICLE_STATUS$]", articleStatus);

		subscriptionSender.setContextCreatorUserPrefix("ARTICLE");
		subscriptionSender.setCreatorUserId(article.getUserId());
		subscriptionSender.setCurrentUserId(userId);
		subscriptionSender.setEntryTitle(articleTitle);
		subscriptionSender.setEntryURL(articleURL);
		subscriptionSender.setFrom(fromAddress, fromName);
		subscriptionSender.setHtmlFormat(true);
		subscriptionSender.setLocalizedBodyMap(localizedBodyMap);
		subscriptionSender.setLocalizedSubjectMap(localizedSubjectMap);
		subscriptionSender.setMailId("journal_article", article.getId());
		subscriptionSender.setNotificationType(notificationType);
		subscriptionSender.setPortletId(portletId);
		subscriptionSender.setReplyToAddress(fromAddress);
		subscriptionSender.setScopeGroupId(article.getGroupId());
		subscriptionSender.setServiceContext(serviceContext);

		subscriptionSender.addPersistedSubscribers(
			JournalFolder.class.getName(), article.getGroupId());

		if (folder != null) {
			subscriptionSender.addPersistedSubscribers(
				JournalFolder.class.getName(), folder.getFolderId());

			for (Long ancestorFolderId : folder.getAncestorFolderIds()) {
				subscriptionSender.addPersistedSubscribers(
					JournalFolder.class.getName(), ancestorFolderId);
			}
		}

		DDMStructure ddmStructure = ddmStructureLocalService.getStructure(
			_portal.getSiteGroupId(article.getGroupId()),
			classNameLocalService.getClassNameId(JournalArticle.class),
			article.getDDMStructureKey(), true);

		subscriptionSender.addPersistedSubscribers(
			DDMStructure.class.getName(), ddmStructure.getStructureId());

		subscriptionSender.addPersistedSubscribers(
			JournalArticle.class.getName(), article.getResourcePrimKey());

		subscriptionSender.flushNotificationsAsync();
	}

	protected void sanitize(
			long companyId, long groupId, long userId, long classPK,
			Map<Locale, String> descriptionMap)
		throws PortalException {

		if (descriptionMap == null) {
			return;
		}

		for (Map.Entry<Locale, String> entry : descriptionMap.entrySet()) {
			String description = SanitizerUtil.sanitize(
				companyId, groupId, userId, JournalArticle.class.getName(),
				classPK, ContentTypes.TEXT_HTML, entry.getValue());

			descriptionMap.put(entry.getKey(), description);
		}
	}

	protected void saveImages(
			boolean smallImage, long smallImageId, File smallImageFile,
			byte[] smallImageBytes)
		throws PortalException {

		if (smallImage) {
			if ((smallImageFile != null) && (smallImageBytes != null)) {
				imageLocalService.updateImage(smallImageId, smallImageBytes);
			}
		}
		else {
			imageLocalService.deleteImage(smallImageId);
		}
	}

	protected BaseModelSearchResult<JournalArticle> searchJournalArticles(
			SearchContext searchContext)
		throws PortalException {

		Indexer<JournalArticle> indexer =
			IndexerRegistryUtil.nullSafeGetIndexer(JournalArticle.class);

		for (int i = 0; i < 10; i++) {
			Hits hits = indexer.search(
				searchContext, JournalUtil.SELECTED_FIELD_NAMES);

			List<JournalArticle> articles = _journalHelper.getArticles(hits);

			if (articles != null) {
				return new BaseModelSearchResult<>(articles, hits.getLength());
			}
		}

		throw new SearchException(
			"Unable to fix the search index after 10 attempts");
	}

	protected void sendEmail(
			JournalArticle article, String articleURL, String emailType,
			ServiceContext serviceContext)
		throws PortalException {

		JournalGroupServiceConfiguration journalGroupServiceConfiguration =
			getJournalGroupServiceConfiguration(article.getGroupId());

		if (emailType.equals("denied") &&
			journalGroupServiceConfiguration.
				emailArticleApprovalDeniedEnabled()) {
		}
		else if (emailType.equals("granted") &&
				 journalGroupServiceConfiguration.
					 emailArticleApprovalGrantedEnabled()) {
		}
		else if (emailType.equals("requested") &&
				 journalGroupServiceConfiguration.
					 emailArticleApprovalRequestedEnabled()) {
		}
		else if (emailType.equals("review") &&
				 journalGroupServiceConfiguration.emailArticleReviewEnabled()) {
		}
		else {
			return;
		}

		Company company = companyLocalService.getCompany(
			article.getCompanyId());

		User user = userLocalService.getUser(article.getUserId());

		String fromName = journalGroupServiceConfiguration.emailFromName();
		String fromAddress =
			journalGroupServiceConfiguration.emailFromAddress();

		String toName = user.getFullName();
		String toAddress = user.getEmailAddress();

		if (emailType.equals("requested")) {
			String tempToName = fromName;
			String tempToAddress = fromAddress;

			fromName = toName;
			fromAddress = toAddress;

			toName = tempToName;
			toAddress = tempToAddress;
		}

		Map<Locale, String> localizedSubjectMap = null;
		Map<Locale, String> localizedBodyMap = null;

		if (emailType.equals("denied")) {
			localizedSubjectMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.
					emailArticleApprovalDeniedSubject());
			localizedBodyMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.
					emailArticleApprovalDeniedBody());
		}
		else if (emailType.equals("granted")) {
			localizedSubjectMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.
					emailArticleApprovalGrantedSubject());
			localizedBodyMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.
					emailArticleApprovalGrantedBody());
		}
		else if (emailType.equals("requested")) {
			localizedSubjectMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.
					emailArticleApprovalRequestedSubject());
			localizedBodyMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.
					emailArticleApprovalRequestedBody());
		}
		else if (emailType.equals("review")) {
			localizedSubjectMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.emailArticleReviewSubject());
			localizedBodyMap = LocalizationUtil.getMap(
				journalGroupServiceConfiguration.emailArticleReviewBody());
		}

		SubscriptionSender subscriptionSender = new SubscriptionSender();

		subscriptionSender.setClassName(JournalArticle.class.getName());
		subscriptionSender.setClassPK(article.getPrimaryKey());
		subscriptionSender.setCompanyId(company.getCompanyId());
		subscriptionSender.setContextAttributes(
			"[$ARTICLE_ID$]", article.getArticleId(), "[$ARTICLE_TITLE$]",
			article.getTitle(serviceContext.getLanguageId()), "[$ARTICLE_URL$]",
			articleURL, "[$ARTICLE_USER_NAME$]", article.getUserName(),
			"[$ARTICLE_VERSION$]", article.getVersion());
		subscriptionSender.setContextCreatorUserPrefix("ARTICLE");
		subscriptionSender.setCreatorUserId(article.getUserId());
		subscriptionSender.setEntryTitle(article.getTitle(user.getLocale()));
		subscriptionSender.setEntryURL(articleURL);
		subscriptionSender.setFrom(fromAddress, fromName);
		subscriptionSender.setHtmlFormat(true);
		subscriptionSender.setLocalizedBodyMap(localizedBodyMap);
		subscriptionSender.setLocalizedSubjectMap(localizedSubjectMap);
		subscriptionSender.setMailId("journal_article", article.getId());

		String portletId = PortletProviderUtil.getPortletId(
			JournalArticle.class.getName(), PortletProvider.Action.EDIT);

		subscriptionSender.setPortletId(portletId);

		subscriptionSender.setScopeGroupId(article.getGroupId());
		subscriptionSender.setServiceContext(serviceContext);

		subscriptionSender.addRuntimeSubscribers(toAddress, toName);

		subscriptionSender.flushNotificationsAsync();
	}

	protected JournalArticle setArticlesExpirationDate(JournalArticle article) {
		if (ExportImportThreadLocal.isImportInProcess()) {
			return article;
		}

		if (!article.isApproved() || (article.getExpirationDate() == null)) {
			return article;
		}

		List<JournalArticle> articles = journalArticlePersistence.findByG_A(
			article.getGroupId(), article.getArticleId());

		Date expirationDate = article.getExpirationDate();

		for (JournalArticle curArticle : articles) {
			curArticle.setExpirationDate(expirationDate);

			curArticle = journalArticleLocalService.updateJournalArticle(
				curArticle);

			if (article.equals(curArticle)) {
				article = curArticle;
			}
		}

		return article;
	}

	protected void startWorkflowInstance(
			long userId, JournalArticle article, ServiceContext serviceContext)
		throws PortalException {

		Map<String, Serializable> workflowContext = new HashMap<>();

		WorkflowHandlerRegistryUtil.startWorkflowInstance(
			article.getCompanyId(), article.getGroupId(), userId,
			JournalArticle.class.getName(), article.getId(), article,
			serviceContext, workflowContext);
	}

	protected void updateDDMFormFieldPredefinedValue(
		DDMFormField ddmFormField, LocalizedValue ddmFormFieldValue) {

		DDMForm ddmForm = ddmFormField.getDDMForm();

		Set<Locale> ddmFormAvailableLocales = ddmForm.getAvailableLocales();

		ddmFormField.setPredefinedValue(ddmFormFieldValue);

		ddmFormAvailableLocales.addAll(ddmFormFieldValue.getAvailableLocales());

		for (Locale locale : ddmFormAvailableLocales) {
			LocalizedValue label = ddmFormField.getLabel();

			Map<Locale, String> labelValues = label.getValues();

			if (!labelValues.containsKey(locale)) {
				label.addString(
					locale, label.getString(label.getDefaultLocale()));
			}

			LocalizedValue style = ddmFormField.getStyle();

			Map<Locale, String> styleValues = style.getValues();

			if (!styleValues.containsKey(locale)) {
				style.addString(
					locale, style.getString(style.getDefaultLocale()));
			}

			LocalizedValue tip = ddmFormField.getTip();

			Map<Locale, String> tipValues = tip.getValues();

			if (!tipValues.containsKey(locale)) {
				tip.addString(locale, tip.getString(tip.getDefaultLocale()));
			}
		}
	}

	protected void updateDDMLinks(
			long id, long groupId, String ddmStructureKey,
			String ddmTemplateKey, boolean incrementVersion)
		throws PortalException {

		DDMStructure ddmStructure = ddmStructureLocalService.getStructure(
			_portal.getSiteGroupId(groupId),
			classNameLocalService.getClassNameId(JournalArticle.class),
			ddmStructureKey, true);

		DDMTemplate ddmTemplate = ddmTemplateLocalService.fetchTemplate(
			_portal.getSiteGroupId(groupId),
			classNameLocalService.getClassNameId(DDMStructure.class),
			ddmTemplateKey, true);

		if (incrementVersion) {
			ddmStorageLinkLocalService.addStorageLink(
				ddmStructure.getClassNameId(), id,
				ddmStructure.getStructureId(), new ServiceContext());

			ddmStructureLinkLocalService.addStructureLink(
				classNameLocalService.getClassNameId(JournalArticle.class), id,
				ddmStructure.getStructureId());

			if (ddmTemplate != null) {
				ddmTemplateLinkLocalService.addTemplateLink(
					classNameLocalService.getClassNameId(JournalArticle.class),
					id, ddmTemplate.getTemplateId());
			}
		}
		else {
			DDMStorageLink ddmStorageLink =
				ddmStorageLinkLocalService.getClassStorageLink(id);

			ddmStorageLink.setStructureId(ddmStructure.getStructureId());

			ddmStorageLinkLocalService.updateDDMStorageLink(ddmStorageLink);

			int count = ddmStructureLinkLocalService.getStructureLinksCount(
				classNameLocalService.getClassNameId(JournalArticle.class), id);

			if (count == 0) {
				ddmStructureLinkLocalService.addStructureLink(
					classNameLocalService.getClassNameId(JournalArticle.class),
					id, ddmStructure.getStructureId());
			}
			else {
				DDMStructureLink ddmStructureLink =
					ddmStructureLinkLocalService.getUniqueStructureLink(
						classNameLocalService.getClassNameId(
							JournalArticle.class),
						id);

				ddmStructureLink.setStructureId(ddmStructure.getStructureId());

				ddmStructureLinkLocalService.updateDDMStructureLink(
					ddmStructureLink);
			}

			if (ddmTemplate != null) {
				ddmTemplateLinkLocalService.updateTemplateLink(
					classNameLocalService.getClassNameId(JournalArticle.class),
					id, ddmTemplate.getTemplateId());
			}
		}
	}

	protected void updateDDMStructurePredefinedValues(
			long ddmStructureId, String content, ServiceContext serviceContext)
		throws PortalException {

		DDMStructure ddmStructure = ddmStructureLocalService.fetchDDMStructure(
			ddmStructureId);

		if (ddmStructure == null) {
			return;
		}

		DDMForm ddmForm = ddmStructure.getDDMForm();

		Map<String, DDMFormField> ddmFormFieldsMap =
			ddmForm.getDDMFormFieldsMap(true);

		Map<String, DDMFormField> fullHierarchyDDMFormFieldsMap =
			ddmStructure.getFullHierarchyDDMFormFieldsMap(true);

		Map<String, LocalizedValue> fieldsValuesMap = createFieldsValuesMap(
			content);

		for (Map.Entry<String, LocalizedValue> fieldValue :
				fieldsValuesMap.entrySet()) {

			String ddmFormFieldName = fieldValue.getKey();
			LocalizedValue ddmFormFieldValue = fieldValue.getValue();

			if (fullHierarchyDDMFormFieldsMap.containsKey(ddmFormFieldName)) {
				updateDDMFormFieldPredefinedValue(
					fullHierarchyDDMFormFieldsMap.get(ddmFormFieldName),
					ddmFormFieldValue);
			}

			if (ddmFormFieldsMap.containsKey(ddmFormFieldName)) {
				updateDDMFormFieldPredefinedValue(
					ddmFormFieldsMap.get(ddmFormFieldName), ddmFormFieldValue);
			}
		}

		boolean indexingEnabled = serviceContext.isIndexingEnabled();

		try {
			serviceContext.setIndexingEnabled(false);

			ddmStructureLocalService.updateStructure(
				serviceContext.getUserId(), ddmStructureId, ddmForm,
				ddmStructure.getDDMFormLayout(), serviceContext);
		}
		finally {
			serviceContext.setIndexingEnabled(indexingEnabled);
		}
	}

	protected void updateFriendlyURLs(
			JournalArticle article, Map<String, String> urlTitleMap,
			ServiceContext serviceContext)
		throws PortalException {

		if (ExportImportThreadLocal.isImportInProcess() ||
			ExportImportThreadLocal.isStagingInProcess()) {

			return;
		}

		List<FriendlyURLEntry> friendlyURLEntries =
			friendlyURLEntryLocalService.getFriendlyURLEntries(
				article.getGroupId(),
				classNameLocalService.getClassNameId(JournalArticle.class),
				article.getResourcePrimKey());

		FriendlyURLEntry newFriendlyURLEntry =
			friendlyURLEntryLocalService.addFriendlyURLEntry(
				article.getGroupId(),
				classNameLocalService.getClassNameId(JournalArticle.class),
				article.getResourcePrimKey(), urlTitleMap, serviceContext);

		for (FriendlyURLEntry friendlyURLEntry : friendlyURLEntries) {
			if (newFriendlyURLEntry.getFriendlyURLEntryId() ==
					friendlyURLEntry.getFriendlyURLEntryId()) {

				continue;
			}

			friendlyURLEntryLocalService.deleteFriendlyURLEntry(
				friendlyURLEntry);
		}
	}

	protected void updatePreviousApprovedArticle(JournalArticle article)
		throws PortalException {

		JournalArticle previousApprovedArticle = getPreviousApprovedArticle(
			article);

		if (previousApprovedArticle.getVersion() == article.getVersion()) {
			assetEntryLocalService.updateVisible(
				JournalArticle.class.getName(), article.getResourcePrimKey(),
				false);
		}
		else {
			AssetEntry assetEntry = assetEntryLocalService.updateEntry(
				JournalArticle.class.getName(), article.getResourcePrimKey(),
				previousApprovedArticle.getDisplayDate(),
				previousApprovedArticle.getExpirationDate(),
				isListable(article), true);

			assetEntry.setModifiedDate(
				previousApprovedArticle.getModifiedDate());
			assetEntry.setTitle(previousApprovedArticle.getTitleMapAsXML());

			assetEntryLocalService.updateAssetEntry(assetEntry);
		}
	}

	protected void updateUrlTitles(
			long groupId, String articleId, String urlTitle)
		throws PortalException {

		JournalArticle firstArticle = journalArticlePersistence.findByG_A_First(
			groupId, articleId, new ArticleVersionComparator(false));

		String firstArticleUrlTitle = firstArticle.getUrlTitle();

		if (firstArticleUrlTitle.equals(urlTitle)) {
			return;
		}

		List<JournalArticle> articles = journalArticlePersistence.findByG_A(
			groupId, articleId);

		for (JournalArticle article : articles) {
			String curArticleUrlTitle = article.getUrlTitle();

			if (!curArticleUrlTitle.equals(urlTitle)) {
				article.setUrlTitle(urlTitle);

				journalArticlePersistence.update(article);
			}
		}
	}

	protected void validate(
			long companyId, long groupId, long classNameId,
			Map<Locale, String> titleMap, String content,
			String ddmStructureKey, String ddmTemplateKey, Date displayDate,
			Date expirationDate, boolean smallImage, String smallImageURL,
			File smallImageFile, byte[] smallImageBytes,
			ServiceContext serviceContext)
		throws PortalException {

		_getModelValidator().validate(
			companyId, groupId, classNameId, titleMap, content, ddmStructureKey,
			ddmTemplateKey, displayDate, expirationDate, smallImage,
			smallImageURL, smallImageFile, smallImageBytes, serviceContext);
	}

	protected void validate(
			long companyId, long groupId, long classNameId, String articleId,
			boolean autoArticleId, double version, Map<Locale, String> titleMap,
			String content, String ddmStructureKey, String ddmTemplateKey,
			Date displayDate, Date expirationDate, boolean smallImage,
			String smallImageURL, File smallImageFile, byte[] smallImageBytes,
			ServiceContext serviceContext)
		throws PortalException {

		_getModelValidator().validate(
			companyId, groupId, classNameId, articleId, autoArticleId, version,
			titleMap, content, ddmStructureKey, ddmTemplateKey, displayDate,
			expirationDate, smallImage, smallImageURL, smallImageFile,
			smallImageBytes, serviceContext);
	}

	protected void validate(String articleId) throws PortalException {
		_getModelValidator().validate(articleId);
	}

	protected void validateContent(String content) throws PortalException {
		_getModelValidator().validateContent(content);
	}

	protected void validateDDMStructureId(
			long groupId, long folderId, String ddmStructureKey)
		throws PortalException {

		_getModelValidator().validateDDMStructureId(
			groupId, folderId, ddmStructureKey);
	}

	protected void validateReferences(
			long groupId, String ddmStructureKey, String ddmTemplateKey,
			String layoutUuid, boolean smallImage, String smallImageURL,
			byte[] smallImageBytes, long smallImageId, String content)
		throws PortalException {

		_getModelValidator().validateReferences(
			groupId, ddmStructureKey, ddmTemplateKey, layoutUuid, smallImage,
			smallImageURL, smallImageBytes, smallImageId, content);
	}

	@Reference
	protected ConfigurationProvider configurationProvider;

	@Reference
	protected DDMStorageLinkLocalService ddmStorageLinkLocalService;

	@Reference
	protected DDMStructureLinkLocalService ddmStructureLinkLocalService;

	@Reference
	protected DDMStructureLocalService ddmStructureLocalService;

	@Reference
	protected DDMTemplateLinkLocalService ddmTemplateLinkLocalService;

	@Reference
	protected DDMTemplateLocalService ddmTemplateLocalService;

	@Reference
	protected FriendlyURLEntryLocalService friendlyURLEntryLocalService;

	private FileEntry _addArticleAttachmentFileEntry(
			JournalArticle article, long folderId, FileEntry fileEntry)
		throws PortalException {

		String fileEntryName = DLUtil.getUniqueFileName(
			fileEntry.getGroupId(), fileEntry.getFolderId(),
			fileEntry.getFileName());

		return PortletFileRepositoryUtil.addPortletFileEntry(
			article.getGroupId(), fileEntry.getUserId(),
			JournalArticle.class.getName(), article.getResourcePrimKey(),
			JournalConstants.SERVICE_NAME, folderId,
			fileEntry.getContentStream(), fileEntryName,
			fileEntry.getMimeType(), false);
	}

	private List<JournalArticleLocalization> _addArticleLocalizedFields(
		long companyId, long articlePK, Map<Locale, String> titleMap,
		Map<Locale, String> descriptionMap) {

		Set<Locale> localeSet = new HashSet<>();

		localeSet.addAll(titleMap.keySet());

		if (descriptionMap != null) {
			localeSet.addAll(descriptionMap.keySet());
		}

		List<JournalArticleLocalization> journalArticleLocalizations =
			new ArrayList<>();

		for (Locale locale : localeSet) {
			String title = titleMap.get(locale);

			String description = null;

			if (descriptionMap != null) {
				description = descriptionMap.get(locale);
			}

			if (Validator.isNull(title) && Validator.isNull(description)) {
				continue;
			}

			JournalArticleLocalization journalArticleLocalization =
				_addArticleLocalizedFields(
					companyId, articlePK, title, description,
					LocaleUtil.toLanguageId(locale));

			journalArticleLocalizations.add(journalArticleLocalization);
		}

		return journalArticleLocalizations;
	}

	private JournalArticleLocalization _addArticleLocalizedFields(
		long companyId, long articlePK, String title, String description,
		String languageId) {

		JournalArticleLocalization journalArticleLocalization =
			journalArticleLocalizationPersistence.fetchByA_L(
				articlePK, languageId);

		if (journalArticleLocalization == null) {
			long journalArticleLocalizationId = counterLocalService.increment();

			journalArticleLocalization =
				journalArticleLocalizationPersistence.create(
					journalArticleLocalizationId);

			journalArticleLocalization.setCompanyId(companyId);
			journalArticleLocalization.setArticlePK(articlePK);
			journalArticleLocalization.setTitle(title);
			journalArticleLocalization.setDescription(description);
			journalArticleLocalization.setLanguageId(languageId);
		}
		else {
			journalArticleLocalization.setTitle(title);
			journalArticleLocalization.setDescription(description);
		}

		return journalArticleLocalizationPersistence.update(
			journalArticleLocalization);
	}

	private Map<Locale, String> _checkFriendlyURLMap(
		Locale defaultLocale, Map<Locale, String> friendlyURLMap,
		Map<Locale, String> titleMap) {

		for (Map.Entry<Locale, String> friendlyURL :
				friendlyURLMap.entrySet()) {

			if (Validator.isNotNull(friendlyURL.getValue())) {
				return friendlyURLMap;
			}
		}

		Map<Locale, String> defaultFriendlyURLMap = new HashMap<>();

		defaultFriendlyURLMap.put(defaultLocale, titleMap.get(defaultLocale));

		return defaultFriendlyURLMap;
	}

	private void _deleteDDMStructurePredefinedValues(
			long groupId, String ddmStructureKey)
		throws PortalException {

		DDMStructure ddmStructure = ddmStructureLocalService.fetchStructure(
			groupId, classNameLocalService.getClassNameId(JournalArticle.class),
			ddmStructureKey, true);

		if (ddmStructure == null) {
			return;
		}

		DDMForm ddmForm = ddmStructure.getDDMForm();

		for (DDMFormField ddmFormField : ddmForm.getDDMFormFields()) {
			ddmFormField.setPredefinedValue(new LocalizedValue());
		}

		ServiceContext serviceContext =
			ServiceContextThreadLocal.getServiceContext();

		boolean indexingEnabled = serviceContext.isIndexingEnabled();

		try {
			serviceContext.setIndexingEnabled(false);

			ddmStructureLocalService.updateStructure(
				serviceContext.getUserId(), ddmStructure.getStructureId(),
				ddmForm, ddmStructure.getDDMFormLayout(), serviceContext);
		}
		finally {
			serviceContext.setIndexingEnabled(indexingEnabled);
		}
	}

	private JournalArticleModelValidator _getModelValidator() {
		ModelValidator<JournalArticle> modelValidator =
			ModelValidatorRegistryUtil.getModelValidator(JournalArticle.class);

		return (JournalArticleModelValidator)modelValidator;
	}

	private int _getUniqueUrlTitleCount(
		long groupId, String articleId, String urlTitle) {

		for (int i = 1;; i++) {
			JournalArticle article = fetchArticleByUrlTitle(groupId, urlTitle);

			if ((article == null) ||
				Objects.equals(articleId, article.getArticleId())) {

				return i - 1;
			}

			String suffix = StringPool.DASH + i;

			String prefix = urlTitle;

			if (urlTitle.length() > suffix.length()) {
				prefix = urlTitle.substring(
					0, urlTitle.length() - suffix.length());
			}

			urlTitle = prefix + suffix;
		}
	}

	private Map<String, String> _getURLTitleMap(
		long groupId, long resourcePrimKey, Map<Locale, String> friendlyURLMap,
		Map<Locale, String> titleMap) {

		Map<String, String> urlTitleMap = new HashMap<>();

		for (Map.Entry<Locale, String> entry : titleMap.entrySet()) {
			String friendlyURL = friendlyURLMap.get(entry.getKey());

			if (Validator.isNull(friendlyURL)) {
				friendlyURL = titleMap.get(entry.getKey());

				if (Validator.isNull(friendlyURL)) {
					continue;
				}
			}

			String urlTitle = friendlyURLEntryLocalService.getUniqueUrlTitle(
				groupId,
				classNameLocalService.getClassNameId(JournalArticle.class),
				resourcePrimKey, friendlyURL);

			urlTitleMap.put(LocaleUtil.toLanguageId(entry.getKey()), urlTitle);
		}

		return urlTitleMap;
	}

	private String _replaceTempImages(JournalArticle article, String content)
		throws PortalException {

		Folder folder = article.addImagesFolder();

		return _attachmentContentUpdater.updateContent(
			content, ContentTypes.TEXT_HTML,
			fileEntry -> _addArticleAttachmentFileEntry(
				article, folder.getFolderId(), fileEntry));
	}

	private List<JournalArticleLocalization> _updateArticleLocalizedFields(
		long companyId, long articleId, Map<Locale, String> titleMap,
		Map<Locale, String> descriptionMap) {

		List<JournalArticleLocalization> oldJournalArticleLocalizations =
			new ArrayList<>(
				journalArticleLocalizationPersistence.findByArticlePK(
					articleId));

		List<JournalArticleLocalization> newJournalArticleLocalizations =
			_addArticleLocalizedFields(
				companyId, articleId, titleMap, descriptionMap);

		oldJournalArticleLocalizations.removeAll(
			newJournalArticleLocalizations);

		for (JournalArticleLocalization oldJournalArticleLocalization :
				oldJournalArticleLocalizations) {

			journalArticleLocalizationPersistence.remove(
				oldJournalArticleLocalization);
		}

		return newJournalArticleLocalizations;
	}

	private JournalArticleLocalization _updateArticleLocalizedFields(
		long companyId, long articleId, String title, String description,
		String languageId) {

		JournalArticleLocalization journalArticleLocalization =
			journalArticleLocalizationPersistence.fetchByA_L(
				articleId, languageId);

		if (journalArticleLocalization == null) {
			return _addArticleLocalizedFields(
				companyId, articleId, title, description, languageId);
		}

		journalArticleLocalization.setTitle(title);
		journalArticleLocalization.setDescription(description);

		return journalArticleLocalizationPersistence.update(
			journalArticleLocalization);
	}

	private static final Log _log = LogFactoryUtil.getLog(
		JournalArticleLocalServiceImpl.class);

	@Reference
	private AttachmentContentUpdater _attachmentContentUpdater;

	@Reference
	private CommentManager _commentManager;

	@Reference
	private DLURLHelper _dlURLHelper;

	@Reference
	private Http _http;

	@Reference
	private JournalArticleResourceLocalService
		_journalArticleResourceLocalService;

	@Reference
	private JournalContentSearchLocalService _journalContentSearchLocalService;

	@Reference
	private JournalDefaultTemplateProvider _journalDefaultTemplateProvider;

	@Reference
	private JournalHelper _journalHelper;

	@Reference
	private Portal _portal;

	private Date _previousCheckDate;

	@Reference
	private SubscriptionLocalService _subscriptionLocalService;

	@Reference
	private TrashHelper _trashHelper;

}