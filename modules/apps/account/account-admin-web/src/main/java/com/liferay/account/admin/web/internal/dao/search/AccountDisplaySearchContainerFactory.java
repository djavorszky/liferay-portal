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

package com.liferay.account.admin.web.internal.dao.search;

import com.liferay.account.admin.web.internal.display.AccountDisplay;
import com.liferay.account.model.AccountEntry;
import com.liferay.account.service.AccountEntryLocalServiceUtil;
import com.liferay.portal.kernel.dao.search.EmptyOnClickRowChecker;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.OrderByComparatorFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.vulcan.util.TransformUtil;

import java.util.List;
import java.util.Objects;

/**
 * @author Pei-Jung Lan
 */
public class AccountDisplaySearchContainerFactory {

	public static SearchContainer create(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse) {

		SearchContainer accountDisplaySearchContainer = new SearchContainer(
			liferayPortletRequest, liferayPortletResponse.createRenderURL(),
			null, "no-accounts-were-found");

		accountDisplaySearchContainer.setId("accounts");

		String orderByCol = ParamUtil.getString(
			liferayPortletRequest, "orderByCol", "name");

		accountDisplaySearchContainer.setOrderByCol(orderByCol);

		String orderByType = ParamUtil.getString(
			liferayPortletRequest, "orderByType", "asc");

		accountDisplaySearchContainer.setOrderByType(orderByType);

		accountDisplaySearchContainer.setOrderByComparator(
			_getOrderByComparator(orderByCol, orderByType));

		accountDisplaySearchContainer.setRowChecker(
			new EmptyOnClickRowChecker(liferayPortletResponse));

		ThemeDisplay themeDisplay =
			(ThemeDisplay)liferayPortletRequest.getAttribute(
				WebKeys.THEME_DISPLAY);

		String navigation = ParamUtil.getString(
			liferayPortletRequest, "navigation", "active");

		List<AccountEntry> accountEntries =
			AccountEntryLocalServiceUtil.getAccountEntries(
				themeDisplay.getCompanyId(), _getStatus(navigation),
				accountDisplaySearchContainer.getStart(),
				accountDisplaySearchContainer.getEnd(),
				accountDisplaySearchContainer.getOrderByComparator());

		List<AccountDisplay> accountDisplays = TransformUtil.transform(
			accountEntries, AccountDisplay::of);

		accountDisplaySearchContainer.setResults(accountDisplays);
		accountDisplaySearchContainer.setTotal(accountDisplays.size());

		return accountDisplaySearchContainer;
	}

	private static OrderByComparator _getOrderByComparator(
		String orderByCol, String orderByType) {

		if (Objects.equals(orderByCol, "name")) {
			return OrderByComparatorFactoryUtil.create(
				"AccountEntry", orderByCol, Objects.equals(orderByType, "asc"));
		}

		return null;
	}

	private static int _getStatus(String navigation) {
		if (Objects.equals(navigation, "inactive")) {
			return WorkflowConstants.STATUS_INACTIVE;
		}

		return WorkflowConstants.STATUS_APPROVED;
	}

}