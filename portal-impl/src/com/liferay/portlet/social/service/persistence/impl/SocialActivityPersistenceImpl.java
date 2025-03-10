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

package com.liferay.portlet.social.service.persistence.impl;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.orm.EntityCache;
import com.liferay.portal.kernel.dao.orm.EntityCacheUtil;
import com.liferay.portal.kernel.dao.orm.FinderCacheUtil;
import com.liferay.portal.kernel.dao.orm.FinderPath;
import com.liferay.portal.kernel.dao.orm.Query;
import com.liferay.portal.kernel.dao.orm.QueryPos;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.Session;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.auth.CompanyThreadLocal;
import com.liferay.portal.kernel.service.persistence.impl.BasePersistenceImpl;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.kernel.util.SetUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portlet.social.model.impl.SocialActivityImpl;
import com.liferay.portlet.social.model.impl.SocialActivityModelImpl;
import com.liferay.social.kernel.exception.NoSuchActivityException;
import com.liferay.social.kernel.model.SocialActivity;
import com.liferay.social.kernel.service.persistence.SocialActivityPersistence;

import java.io.Serializable;

import java.lang.reflect.InvocationHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The persistence implementation for the social activity service.
 *
 * <p>
 * Caching information and settings can be found in <code>portal.properties</code>
 * </p>
 *
 * @author Brian Wing Shun Chan
 * @generated
 */
public class SocialActivityPersistenceImpl
	extends BasePersistenceImpl<SocialActivity>
	implements SocialActivityPersistence {

	/**
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this class directly. Always use <code>SocialActivityUtil</code> to access the social activity persistence. Modify <code>service.xml</code> and rerun ServiceBuilder to regenerate this class.
	 */
	public static final String FINDER_CLASS_NAME_ENTITY =
		SocialActivityImpl.class.getName();

	public static final String FINDER_CLASS_NAME_LIST_WITH_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List1";

	public static final String FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION =
		FINDER_CLASS_NAME_ENTITY + ".List2";

	private FinderPath _finderPathWithPaginationFindAll;
	private FinderPath _finderPathWithoutPaginationFindAll;
	private FinderPath _finderPathCountAll;
	private FinderPath _finderPathWithPaginationFindByGroupId;
	private FinderPath _finderPathWithoutPaginationFindByGroupId;
	private FinderPath _finderPathCountByGroupId;

	/**
	 * Returns all the social activities where groupId = &#63;.
	 *
	 * @param groupId the group ID
	 * @return the matching social activities
	 */
	@Override
	public List<SocialActivity> findByGroupId(long groupId) {
		return findByGroupId(
			groupId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the social activities where groupId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @return the range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByGroupId(
		long groupId, int start, int end) {

		return findByGroupId(groupId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the social activities where groupId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByGroupId(
		long groupId, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator) {

		return findByGroupId(groupId, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the social activities where groupId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByGroupId(
		long groupId, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator,
		boolean useFinderCache) {

		boolean pagination = true;
		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			pagination = false;

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByGroupId;
				finderArgs = new Object[] {groupId};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByGroupId;
			finderArgs = new Object[] {groupId, start, end, orderByComparator};
		}

		List<SocialActivity> list = null;

		if (useFinderCache) {
			list = (List<SocialActivity>)FinderCacheUtil.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (SocialActivity socialActivity : list) {
					if (groupId != socialActivity.getGroupId()) {
						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler query = null;

			if (orderByComparator != null) {
				query = new StringBundler(
					3 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				query = new StringBundler(3);
			}

			query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_GROUPID_GROUPID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					query, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else if (pagination) {
				query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
			}

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(groupId);

				if (!pagination) {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end, false);

					Collections.sort(list);

					list = Collections.unmodifiableList(list);
				}
				else {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end);
				}

				cacheResult(list);

				if (useFinderCache) {
					FinderCacheUtil.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception e) {
				if (useFinderCache) {
					FinderCacheUtil.removeResult(finderPath, finderArgs);
				}

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first social activity in the ordered set where groupId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByGroupId_First(
			long groupId, OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByGroupId_First(
			groupId, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(4);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("groupId=");
		msg.append(groupId);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the first social activity in the ordered set where groupId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByGroupId_First(
		long groupId, OrderByComparator<SocialActivity> orderByComparator) {

		List<SocialActivity> list = findByGroupId(
			groupId, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last social activity in the ordered set where groupId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByGroupId_Last(
			long groupId, OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByGroupId_Last(
			groupId, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(4);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("groupId=");
		msg.append(groupId);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the last social activity in the ordered set where groupId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByGroupId_Last(
		long groupId, OrderByComparator<SocialActivity> orderByComparator) {

		int count = countByGroupId(groupId);

		if (count == 0) {
			return null;
		}

		List<SocialActivity> list = findByGroupId(
			groupId, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the social activities before and after the current social activity in the ordered set where groupId = &#63;.
	 *
	 * @param activityId the primary key of the current social activity
	 * @param groupId the group ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next social activity
	 * @throws NoSuchActivityException if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity[] findByGroupId_PrevAndNext(
			long activityId, long groupId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = findByPrimaryKey(activityId);

		Session session = null;

		try {
			session = openSession();

			SocialActivity[] array = new SocialActivityImpl[3];

			array[0] = getByGroupId_PrevAndNext(
				session, socialActivity, groupId, orderByComparator, true);

			array[1] = socialActivity;

			array[2] = getByGroupId_PrevAndNext(
				session, socialActivity, groupId, orderByComparator, false);

			return array;
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	protected SocialActivity getByGroupId_PrevAndNext(
		Session session, SocialActivity socialActivity, long groupId,
		OrderByComparator<SocialActivity> orderByComparator, boolean previous) {

		StringBundler query = null;

		if (orderByComparator != null) {
			query = new StringBundler(
				4 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			query = new StringBundler(3);
		}

		query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

		query.append(_FINDER_COLUMN_GROUPID_GROUPID_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				query.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						query.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN);
					}
					else {
						query.append(WHERE_LESSER_THAN);
					}
				}
			}

			query.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						query.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC);
					}
					else {
						query.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
		}

		String sql = query.toString();

		Query q = session.createQuery(sql);

		q.setFirstResult(0);
		q.setMaxResults(2);

		QueryPos qPos = QueryPos.getInstance(q);

		qPos.add(groupId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						socialActivity)) {

				qPos.add(orderByConditionValue);
			}
		}

		List<SocialActivity> list = q.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the social activities where groupId = &#63; from the database.
	 *
	 * @param groupId the group ID
	 */
	@Override
	public void removeByGroupId(long groupId) {
		for (SocialActivity socialActivity :
				findByGroupId(
					groupId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)) {

			remove(socialActivity);
		}
	}

	/**
	 * Returns the number of social activities where groupId = &#63;.
	 *
	 * @param groupId the group ID
	 * @return the number of matching social activities
	 */
	@Override
	public int countByGroupId(long groupId) {
		FinderPath finderPath = _finderPathCountByGroupId;

		Object[] finderArgs = new Object[] {groupId};

		Long count = (Long)FinderCacheUtil.getResult(
			finderPath, finderArgs, this);

		if (count == null) {
			StringBundler query = new StringBundler(2);

			query.append(_SQL_COUNT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_GROUPID_GROUPID_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(groupId);

				count = (Long)q.uniqueResult();

				FinderCacheUtil.putResult(finderPath, finderArgs, count);
			}
			catch (Exception e) {
				FinderCacheUtil.removeResult(finderPath, finderArgs);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_GROUPID_GROUPID_2 =
		"socialActivity.groupId = ?";

	private FinderPath _finderPathWithPaginationFindByCompanyId;
	private FinderPath _finderPathWithoutPaginationFindByCompanyId;
	private FinderPath _finderPathCountByCompanyId;

	/**
	 * Returns all the social activities where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @return the matching social activities
	 */
	@Override
	public List<SocialActivity> findByCompanyId(long companyId) {
		return findByCompanyId(
			companyId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the social activities where companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @return the range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByCompanyId(
		long companyId, int start, int end) {

		return findByCompanyId(companyId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the social activities where companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByCompanyId(
		long companyId, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator) {

		return findByCompanyId(companyId, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the social activities where companyId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param companyId the company ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByCompanyId(
		long companyId, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator,
		boolean useFinderCache) {

		boolean pagination = true;
		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			pagination = false;

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByCompanyId;
				finderArgs = new Object[] {companyId};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByCompanyId;
			finderArgs = new Object[] {
				companyId, start, end, orderByComparator
			};
		}

		List<SocialActivity> list = null;

		if (useFinderCache) {
			list = (List<SocialActivity>)FinderCacheUtil.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (SocialActivity socialActivity : list) {
					if (companyId != socialActivity.getCompanyId()) {
						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler query = null;

			if (orderByComparator != null) {
				query = new StringBundler(
					3 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				query = new StringBundler(3);
			}

			query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_COMPANYID_COMPANYID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					query, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else if (pagination) {
				query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
			}

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(companyId);

				if (!pagination) {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end, false);

					Collections.sort(list);

					list = Collections.unmodifiableList(list);
				}
				else {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end);
				}

				cacheResult(list);

				if (useFinderCache) {
					FinderCacheUtil.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception e) {
				if (useFinderCache) {
					FinderCacheUtil.removeResult(finderPath, finderArgs);
				}

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first social activity in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByCompanyId_First(
			long companyId, OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByCompanyId_First(
			companyId, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(4);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("companyId=");
		msg.append(companyId);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the first social activity in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByCompanyId_First(
		long companyId, OrderByComparator<SocialActivity> orderByComparator) {

		List<SocialActivity> list = findByCompanyId(
			companyId, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last social activity in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByCompanyId_Last(
			long companyId, OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByCompanyId_Last(
			companyId, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(4);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("companyId=");
		msg.append(companyId);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the last social activity in the ordered set where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByCompanyId_Last(
		long companyId, OrderByComparator<SocialActivity> orderByComparator) {

		int count = countByCompanyId(companyId);

		if (count == 0) {
			return null;
		}

		List<SocialActivity> list = findByCompanyId(
			companyId, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the social activities before and after the current social activity in the ordered set where companyId = &#63;.
	 *
	 * @param activityId the primary key of the current social activity
	 * @param companyId the company ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next social activity
	 * @throws NoSuchActivityException if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity[] findByCompanyId_PrevAndNext(
			long activityId, long companyId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = findByPrimaryKey(activityId);

		Session session = null;

		try {
			session = openSession();

			SocialActivity[] array = new SocialActivityImpl[3];

			array[0] = getByCompanyId_PrevAndNext(
				session, socialActivity, companyId, orderByComparator, true);

			array[1] = socialActivity;

			array[2] = getByCompanyId_PrevAndNext(
				session, socialActivity, companyId, orderByComparator, false);

			return array;
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	protected SocialActivity getByCompanyId_PrevAndNext(
		Session session, SocialActivity socialActivity, long companyId,
		OrderByComparator<SocialActivity> orderByComparator, boolean previous) {

		StringBundler query = null;

		if (orderByComparator != null) {
			query = new StringBundler(
				4 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			query = new StringBundler(3);
		}

		query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

		query.append(_FINDER_COLUMN_COMPANYID_COMPANYID_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				query.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						query.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN);
					}
					else {
						query.append(WHERE_LESSER_THAN);
					}
				}
			}

			query.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						query.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC);
					}
					else {
						query.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
		}

		String sql = query.toString();

		Query q = session.createQuery(sql);

		q.setFirstResult(0);
		q.setMaxResults(2);

		QueryPos qPos = QueryPos.getInstance(q);

		qPos.add(companyId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						socialActivity)) {

				qPos.add(orderByConditionValue);
			}
		}

		List<SocialActivity> list = q.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the social activities where companyId = &#63; from the database.
	 *
	 * @param companyId the company ID
	 */
	@Override
	public void removeByCompanyId(long companyId) {
		for (SocialActivity socialActivity :
				findByCompanyId(
					companyId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)) {

			remove(socialActivity);
		}
	}

	/**
	 * Returns the number of social activities where companyId = &#63;.
	 *
	 * @param companyId the company ID
	 * @return the number of matching social activities
	 */
	@Override
	public int countByCompanyId(long companyId) {
		FinderPath finderPath = _finderPathCountByCompanyId;

		Object[] finderArgs = new Object[] {companyId};

		Long count = (Long)FinderCacheUtil.getResult(
			finderPath, finderArgs, this);

		if (count == null) {
			StringBundler query = new StringBundler(2);

			query.append(_SQL_COUNT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_COMPANYID_COMPANYID_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(companyId);

				count = (Long)q.uniqueResult();

				FinderCacheUtil.putResult(finderPath, finderArgs, count);
			}
			catch (Exception e) {
				FinderCacheUtil.removeResult(finderPath, finderArgs);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_COMPANYID_COMPANYID_2 =
		"socialActivity.companyId = ?";

	private FinderPath _finderPathWithPaginationFindByUserId;
	private FinderPath _finderPathWithoutPaginationFindByUserId;
	private FinderPath _finderPathCountByUserId;

	/**
	 * Returns all the social activities where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @return the matching social activities
	 */
	@Override
	public List<SocialActivity> findByUserId(long userId) {
		return findByUserId(userId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the social activities where userId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param userId the user ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @return the range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByUserId(long userId, int start, int end) {
		return findByUserId(userId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the social activities where userId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param userId the user ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByUserId(
		long userId, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator) {

		return findByUserId(userId, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the social activities where userId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param userId the user ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByUserId(
		long userId, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator,
		boolean useFinderCache) {

		boolean pagination = true;
		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			pagination = false;

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByUserId;
				finderArgs = new Object[] {userId};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByUserId;
			finderArgs = new Object[] {userId, start, end, orderByComparator};
		}

		List<SocialActivity> list = null;

		if (useFinderCache) {
			list = (List<SocialActivity>)FinderCacheUtil.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (SocialActivity socialActivity : list) {
					if (userId != socialActivity.getUserId()) {
						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler query = null;

			if (orderByComparator != null) {
				query = new StringBundler(
					3 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				query = new StringBundler(3);
			}

			query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_USERID_USERID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					query, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else if (pagination) {
				query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
			}

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(userId);

				if (!pagination) {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end, false);

					Collections.sort(list);

					list = Collections.unmodifiableList(list);
				}
				else {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end);
				}

				cacheResult(list);

				if (useFinderCache) {
					FinderCacheUtil.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception e) {
				if (useFinderCache) {
					FinderCacheUtil.removeResult(finderPath, finderArgs);
				}

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first social activity in the ordered set where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByUserId_First(
			long userId, OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByUserId_First(
			userId, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(4);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("userId=");
		msg.append(userId);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the first social activity in the ordered set where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByUserId_First(
		long userId, OrderByComparator<SocialActivity> orderByComparator) {

		List<SocialActivity> list = findByUserId(
			userId, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last social activity in the ordered set where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByUserId_Last(
			long userId, OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByUserId_Last(
			userId, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(4);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("userId=");
		msg.append(userId);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the last social activity in the ordered set where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByUserId_Last(
		long userId, OrderByComparator<SocialActivity> orderByComparator) {

		int count = countByUserId(userId);

		if (count == 0) {
			return null;
		}

		List<SocialActivity> list = findByUserId(
			userId, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the social activities before and after the current social activity in the ordered set where userId = &#63;.
	 *
	 * @param activityId the primary key of the current social activity
	 * @param userId the user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next social activity
	 * @throws NoSuchActivityException if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity[] findByUserId_PrevAndNext(
			long activityId, long userId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = findByPrimaryKey(activityId);

		Session session = null;

		try {
			session = openSession();

			SocialActivity[] array = new SocialActivityImpl[3];

			array[0] = getByUserId_PrevAndNext(
				session, socialActivity, userId, orderByComparator, true);

			array[1] = socialActivity;

			array[2] = getByUserId_PrevAndNext(
				session, socialActivity, userId, orderByComparator, false);

			return array;
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	protected SocialActivity getByUserId_PrevAndNext(
		Session session, SocialActivity socialActivity, long userId,
		OrderByComparator<SocialActivity> orderByComparator, boolean previous) {

		StringBundler query = null;

		if (orderByComparator != null) {
			query = new StringBundler(
				4 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			query = new StringBundler(3);
		}

		query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

		query.append(_FINDER_COLUMN_USERID_USERID_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				query.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						query.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN);
					}
					else {
						query.append(WHERE_LESSER_THAN);
					}
				}
			}

			query.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						query.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC);
					}
					else {
						query.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
		}

		String sql = query.toString();

		Query q = session.createQuery(sql);

		q.setFirstResult(0);
		q.setMaxResults(2);

		QueryPos qPos = QueryPos.getInstance(q);

		qPos.add(userId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						socialActivity)) {

				qPos.add(orderByConditionValue);
			}
		}

		List<SocialActivity> list = q.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the social activities where userId = &#63; from the database.
	 *
	 * @param userId the user ID
	 */
	@Override
	public void removeByUserId(long userId) {
		for (SocialActivity socialActivity :
				findByUserId(
					userId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)) {

			remove(socialActivity);
		}
	}

	/**
	 * Returns the number of social activities where userId = &#63;.
	 *
	 * @param userId the user ID
	 * @return the number of matching social activities
	 */
	@Override
	public int countByUserId(long userId) {
		FinderPath finderPath = _finderPathCountByUserId;

		Object[] finderArgs = new Object[] {userId};

		Long count = (Long)FinderCacheUtil.getResult(
			finderPath, finderArgs, this);

		if (count == null) {
			StringBundler query = new StringBundler(2);

			query.append(_SQL_COUNT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_USERID_USERID_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(userId);

				count = (Long)q.uniqueResult();

				FinderCacheUtil.putResult(finderPath, finderArgs, count);
			}
			catch (Exception e) {
				FinderCacheUtil.removeResult(finderPath, finderArgs);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_USERID_USERID_2 =
		"socialActivity.userId = ?";

	private FinderPath _finderPathWithPaginationFindByActivitySetId;
	private FinderPath _finderPathWithoutPaginationFindByActivitySetId;
	private FinderPath _finderPathCountByActivitySetId;

	/**
	 * Returns all the social activities where activitySetId = &#63;.
	 *
	 * @param activitySetId the activity set ID
	 * @return the matching social activities
	 */
	@Override
	public List<SocialActivity> findByActivitySetId(long activitySetId) {
		return findByActivitySetId(
			activitySetId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the social activities where activitySetId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param activitySetId the activity set ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @return the range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByActivitySetId(
		long activitySetId, int start, int end) {

		return findByActivitySetId(activitySetId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the social activities where activitySetId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param activitySetId the activity set ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByActivitySetId(
		long activitySetId, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator) {

		return findByActivitySetId(
			activitySetId, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the social activities where activitySetId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param activitySetId the activity set ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByActivitySetId(
		long activitySetId, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator,
		boolean useFinderCache) {

		boolean pagination = true;
		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			pagination = false;

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByActivitySetId;
				finderArgs = new Object[] {activitySetId};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByActivitySetId;
			finderArgs = new Object[] {
				activitySetId, start, end, orderByComparator
			};
		}

		List<SocialActivity> list = null;

		if (useFinderCache) {
			list = (List<SocialActivity>)FinderCacheUtil.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (SocialActivity socialActivity : list) {
					if (activitySetId != socialActivity.getActivitySetId()) {
						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler query = null;

			if (orderByComparator != null) {
				query = new StringBundler(
					3 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				query = new StringBundler(3);
			}

			query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_ACTIVITYSETID_ACTIVITYSETID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					query, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else if (pagination) {
				query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
			}

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(activitySetId);

				if (!pagination) {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end, false);

					Collections.sort(list);

					list = Collections.unmodifiableList(list);
				}
				else {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end);
				}

				cacheResult(list);

				if (useFinderCache) {
					FinderCacheUtil.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception e) {
				if (useFinderCache) {
					FinderCacheUtil.removeResult(finderPath, finderArgs);
				}

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first social activity in the ordered set where activitySetId = &#63;.
	 *
	 * @param activitySetId the activity set ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByActivitySetId_First(
			long activitySetId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByActivitySetId_First(
			activitySetId, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(4);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("activitySetId=");
		msg.append(activitySetId);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the first social activity in the ordered set where activitySetId = &#63;.
	 *
	 * @param activitySetId the activity set ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByActivitySetId_First(
		long activitySetId,
		OrderByComparator<SocialActivity> orderByComparator) {

		List<SocialActivity> list = findByActivitySetId(
			activitySetId, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last social activity in the ordered set where activitySetId = &#63;.
	 *
	 * @param activitySetId the activity set ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByActivitySetId_Last(
			long activitySetId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByActivitySetId_Last(
			activitySetId, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(4);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("activitySetId=");
		msg.append(activitySetId);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the last social activity in the ordered set where activitySetId = &#63;.
	 *
	 * @param activitySetId the activity set ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByActivitySetId_Last(
		long activitySetId,
		OrderByComparator<SocialActivity> orderByComparator) {

		int count = countByActivitySetId(activitySetId);

		if (count == 0) {
			return null;
		}

		List<SocialActivity> list = findByActivitySetId(
			activitySetId, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the social activities before and after the current social activity in the ordered set where activitySetId = &#63;.
	 *
	 * @param activityId the primary key of the current social activity
	 * @param activitySetId the activity set ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next social activity
	 * @throws NoSuchActivityException if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity[] findByActivitySetId_PrevAndNext(
			long activityId, long activitySetId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = findByPrimaryKey(activityId);

		Session session = null;

		try {
			session = openSession();

			SocialActivity[] array = new SocialActivityImpl[3];

			array[0] = getByActivitySetId_PrevAndNext(
				session, socialActivity, activitySetId, orderByComparator,
				true);

			array[1] = socialActivity;

			array[2] = getByActivitySetId_PrevAndNext(
				session, socialActivity, activitySetId, orderByComparator,
				false);

			return array;
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	protected SocialActivity getByActivitySetId_PrevAndNext(
		Session session, SocialActivity socialActivity, long activitySetId,
		OrderByComparator<SocialActivity> orderByComparator, boolean previous) {

		StringBundler query = null;

		if (orderByComparator != null) {
			query = new StringBundler(
				4 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			query = new StringBundler(3);
		}

		query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

		query.append(_FINDER_COLUMN_ACTIVITYSETID_ACTIVITYSETID_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				query.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						query.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN);
					}
					else {
						query.append(WHERE_LESSER_THAN);
					}
				}
			}

			query.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						query.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC);
					}
					else {
						query.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
		}

		String sql = query.toString();

		Query q = session.createQuery(sql);

		q.setFirstResult(0);
		q.setMaxResults(2);

		QueryPos qPos = QueryPos.getInstance(q);

		qPos.add(activitySetId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						socialActivity)) {

				qPos.add(orderByConditionValue);
			}
		}

		List<SocialActivity> list = q.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the social activities where activitySetId = &#63; from the database.
	 *
	 * @param activitySetId the activity set ID
	 */
	@Override
	public void removeByActivitySetId(long activitySetId) {
		for (SocialActivity socialActivity :
				findByActivitySetId(
					activitySetId, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					null)) {

			remove(socialActivity);
		}
	}

	/**
	 * Returns the number of social activities where activitySetId = &#63;.
	 *
	 * @param activitySetId the activity set ID
	 * @return the number of matching social activities
	 */
	@Override
	public int countByActivitySetId(long activitySetId) {
		FinderPath finderPath = _finderPathCountByActivitySetId;

		Object[] finderArgs = new Object[] {activitySetId};

		Long count = (Long)FinderCacheUtil.getResult(
			finderPath, finderArgs, this);

		if (count == null) {
			StringBundler query = new StringBundler(2);

			query.append(_SQL_COUNT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_ACTIVITYSETID_ACTIVITYSETID_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(activitySetId);

				count = (Long)q.uniqueResult();

				FinderCacheUtil.putResult(finderPath, finderArgs, count);
			}
			catch (Exception e) {
				FinderCacheUtil.removeResult(finderPath, finderArgs);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_ACTIVITYSETID_ACTIVITYSETID_2 =
		"socialActivity.activitySetId = ?";

	private FinderPath _finderPathFetchByMirrorActivityId;
	private FinderPath _finderPathCountByMirrorActivityId;

	/**
	 * Returns the social activity where mirrorActivityId = &#63; or throws a <code>NoSuchActivityException</code> if it could not be found.
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @return the matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByMirrorActivityId(long mirrorActivityId)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByMirrorActivityId(
			mirrorActivityId);

		if (socialActivity == null) {
			StringBundler msg = new StringBundler(4);

			msg.append(_NO_SUCH_ENTITY_WITH_KEY);

			msg.append("mirrorActivityId=");
			msg.append(mirrorActivityId);

			msg.append("}");

			if (_log.isDebugEnabled()) {
				_log.debug(msg.toString());
			}

			throw new NoSuchActivityException(msg.toString());
		}

		return socialActivity;
	}

	/**
	 * Returns the social activity where mirrorActivityId = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @return the matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByMirrorActivityId(long mirrorActivityId) {
		return fetchByMirrorActivityId(mirrorActivityId, true);
	}

	/**
	 * Returns the social activity where mirrorActivityId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByMirrorActivityId(
		long mirrorActivityId, boolean useFinderCache) {

		Object[] finderArgs = null;

		if (useFinderCache) {
			finderArgs = new Object[] {mirrorActivityId};
		}

		Object result = null;

		if (useFinderCache) {
			result = FinderCacheUtil.getResult(
				_finderPathFetchByMirrorActivityId, finderArgs, this);
		}

		if (result instanceof SocialActivity) {
			SocialActivity socialActivity = (SocialActivity)result;

			if (mirrorActivityId != socialActivity.getMirrorActivityId()) {
				result = null;
			}
		}

		if (result == null) {
			StringBundler query = new StringBundler(3);

			query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_MIRRORACTIVITYID_MIRRORACTIVITYID_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(mirrorActivityId);

				List<SocialActivity> list = q.list();

				if (list.isEmpty()) {
					if (useFinderCache) {
						FinderCacheUtil.putResult(
							_finderPathFetchByMirrorActivityId, finderArgs,
							list);
					}
				}
				else {
					if (list.size() > 1) {
						Collections.sort(list, Collections.reverseOrder());

						if (_log.isWarnEnabled()) {
							if (!useFinderCache) {
								finderArgs = new Object[] {mirrorActivityId};
							}

							_log.warn(
								"SocialActivityPersistenceImpl.fetchByMirrorActivityId(long, boolean) with parameters (" +
									StringUtil.merge(finderArgs) +
										") yields a result set with more than 1 result. This violates the logical unique restriction. There is no order guarantee on which result is returned by this finder.");
						}
					}

					SocialActivity socialActivity = list.get(0);

					result = socialActivity;

					cacheResult(socialActivity);
				}
			}
			catch (Exception e) {
				if (useFinderCache) {
					FinderCacheUtil.removeResult(
						_finderPathFetchByMirrorActivityId, finderArgs);
				}

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		if (result instanceof List<?>) {
			return null;
		}
		else {
			return (SocialActivity)result;
		}
	}

	/**
	 * Removes the social activity where mirrorActivityId = &#63; from the database.
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @return the social activity that was removed
	 */
	@Override
	public SocialActivity removeByMirrorActivityId(long mirrorActivityId)
		throws NoSuchActivityException {

		SocialActivity socialActivity = findByMirrorActivityId(
			mirrorActivityId);

		return remove(socialActivity);
	}

	/**
	 * Returns the number of social activities where mirrorActivityId = &#63;.
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @return the number of matching social activities
	 */
	@Override
	public int countByMirrorActivityId(long mirrorActivityId) {
		FinderPath finderPath = _finderPathCountByMirrorActivityId;

		Object[] finderArgs = new Object[] {mirrorActivityId};

		Long count = (Long)FinderCacheUtil.getResult(
			finderPath, finderArgs, this);

		if (count == null) {
			StringBundler query = new StringBundler(2);

			query.append(_SQL_COUNT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_MIRRORACTIVITYID_MIRRORACTIVITYID_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(mirrorActivityId);

				count = (Long)q.uniqueResult();

				FinderCacheUtil.putResult(finderPath, finderArgs, count);
			}
			catch (Exception e) {
				FinderCacheUtil.removeResult(finderPath, finderArgs);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String
		_FINDER_COLUMN_MIRRORACTIVITYID_MIRRORACTIVITYID_2 =
			"socialActivity.mirrorActivityId = ?";

	private FinderPath _finderPathWithPaginationFindByClassNameId;
	private FinderPath _finderPathWithoutPaginationFindByClassNameId;
	private FinderPath _finderPathCountByClassNameId;

	/**
	 * Returns all the social activities where classNameId = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @return the matching social activities
	 */
	@Override
	public List<SocialActivity> findByClassNameId(long classNameId) {
		return findByClassNameId(
			classNameId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the social activities where classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @return the range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByClassNameId(
		long classNameId, int start, int end) {

		return findByClassNameId(classNameId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the social activities where classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByClassNameId(
		long classNameId, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator) {

		return findByClassNameId(
			classNameId, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the social activities where classNameId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByClassNameId(
		long classNameId, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator,
		boolean useFinderCache) {

		boolean pagination = true;
		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			pagination = false;

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByClassNameId;
				finderArgs = new Object[] {classNameId};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByClassNameId;
			finderArgs = new Object[] {
				classNameId, start, end, orderByComparator
			};
		}

		List<SocialActivity> list = null;

		if (useFinderCache) {
			list = (List<SocialActivity>)FinderCacheUtil.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (SocialActivity socialActivity : list) {
					if (classNameId != socialActivity.getClassNameId()) {
						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler query = null;

			if (orderByComparator != null) {
				query = new StringBundler(
					3 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				query = new StringBundler(3);
			}

			query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_CLASSNAMEID_CLASSNAMEID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					query, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else if (pagination) {
				query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
			}

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(classNameId);

				if (!pagination) {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end, false);

					Collections.sort(list);

					list = Collections.unmodifiableList(list);
				}
				else {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end);
				}

				cacheResult(list);

				if (useFinderCache) {
					FinderCacheUtil.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception e) {
				if (useFinderCache) {
					FinderCacheUtil.removeResult(finderPath, finderArgs);
				}

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first social activity in the ordered set where classNameId = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByClassNameId_First(
			long classNameId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByClassNameId_First(
			classNameId, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(4);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("classNameId=");
		msg.append(classNameId);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the first social activity in the ordered set where classNameId = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByClassNameId_First(
		long classNameId, OrderByComparator<SocialActivity> orderByComparator) {

		List<SocialActivity> list = findByClassNameId(
			classNameId, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last social activity in the ordered set where classNameId = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByClassNameId_Last(
			long classNameId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByClassNameId_Last(
			classNameId, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(4);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("classNameId=");
		msg.append(classNameId);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the last social activity in the ordered set where classNameId = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByClassNameId_Last(
		long classNameId, OrderByComparator<SocialActivity> orderByComparator) {

		int count = countByClassNameId(classNameId);

		if (count == 0) {
			return null;
		}

		List<SocialActivity> list = findByClassNameId(
			classNameId, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the social activities before and after the current social activity in the ordered set where classNameId = &#63;.
	 *
	 * @param activityId the primary key of the current social activity
	 * @param classNameId the class name ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next social activity
	 * @throws NoSuchActivityException if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity[] findByClassNameId_PrevAndNext(
			long activityId, long classNameId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = findByPrimaryKey(activityId);

		Session session = null;

		try {
			session = openSession();

			SocialActivity[] array = new SocialActivityImpl[3];

			array[0] = getByClassNameId_PrevAndNext(
				session, socialActivity, classNameId, orderByComparator, true);

			array[1] = socialActivity;

			array[2] = getByClassNameId_PrevAndNext(
				session, socialActivity, classNameId, orderByComparator, false);

			return array;
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	protected SocialActivity getByClassNameId_PrevAndNext(
		Session session, SocialActivity socialActivity, long classNameId,
		OrderByComparator<SocialActivity> orderByComparator, boolean previous) {

		StringBundler query = null;

		if (orderByComparator != null) {
			query = new StringBundler(
				4 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			query = new StringBundler(3);
		}

		query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

		query.append(_FINDER_COLUMN_CLASSNAMEID_CLASSNAMEID_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				query.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						query.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN);
					}
					else {
						query.append(WHERE_LESSER_THAN);
					}
				}
			}

			query.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						query.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC);
					}
					else {
						query.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
		}

		String sql = query.toString();

		Query q = session.createQuery(sql);

		q.setFirstResult(0);
		q.setMaxResults(2);

		QueryPos qPos = QueryPos.getInstance(q);

		qPos.add(classNameId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						socialActivity)) {

				qPos.add(orderByConditionValue);
			}
		}

		List<SocialActivity> list = q.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the social activities where classNameId = &#63; from the database.
	 *
	 * @param classNameId the class name ID
	 */
	@Override
	public void removeByClassNameId(long classNameId) {
		for (SocialActivity socialActivity :
				findByClassNameId(
					classNameId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)) {

			remove(socialActivity);
		}
	}

	/**
	 * Returns the number of social activities where classNameId = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @return the number of matching social activities
	 */
	@Override
	public int countByClassNameId(long classNameId) {
		FinderPath finderPath = _finderPathCountByClassNameId;

		Object[] finderArgs = new Object[] {classNameId};

		Long count = (Long)FinderCacheUtil.getResult(
			finderPath, finderArgs, this);

		if (count == null) {
			StringBundler query = new StringBundler(2);

			query.append(_SQL_COUNT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_CLASSNAMEID_CLASSNAMEID_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(classNameId);

				count = (Long)q.uniqueResult();

				FinderCacheUtil.putResult(finderPath, finderArgs, count);
			}
			catch (Exception e) {
				FinderCacheUtil.removeResult(finderPath, finderArgs);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_CLASSNAMEID_CLASSNAMEID_2 =
		"socialActivity.classNameId = ?";

	private FinderPath _finderPathWithPaginationFindByReceiverUserId;
	private FinderPath _finderPathWithoutPaginationFindByReceiverUserId;
	private FinderPath _finderPathCountByReceiverUserId;

	/**
	 * Returns all the social activities where receiverUserId = &#63;.
	 *
	 * @param receiverUserId the receiver user ID
	 * @return the matching social activities
	 */
	@Override
	public List<SocialActivity> findByReceiverUserId(long receiverUserId) {
		return findByReceiverUserId(
			receiverUserId, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the social activities where receiverUserId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param receiverUserId the receiver user ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @return the range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByReceiverUserId(
		long receiverUserId, int start, int end) {

		return findByReceiverUserId(receiverUserId, start, end, null);
	}

	/**
	 * Returns an ordered range of all the social activities where receiverUserId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param receiverUserId the receiver user ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByReceiverUserId(
		long receiverUserId, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator) {

		return findByReceiverUserId(
			receiverUserId, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the social activities where receiverUserId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param receiverUserId the receiver user ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByReceiverUserId(
		long receiverUserId, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator,
		boolean useFinderCache) {

		boolean pagination = true;
		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			pagination = false;

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByReceiverUserId;
				finderArgs = new Object[] {receiverUserId};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByReceiverUserId;
			finderArgs = new Object[] {
				receiverUserId, start, end, orderByComparator
			};
		}

		List<SocialActivity> list = null;

		if (useFinderCache) {
			list = (List<SocialActivity>)FinderCacheUtil.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (SocialActivity socialActivity : list) {
					if (receiverUserId != socialActivity.getReceiverUserId()) {
						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler query = null;

			if (orderByComparator != null) {
				query = new StringBundler(
					3 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				query = new StringBundler(3);
			}

			query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_RECEIVERUSERID_RECEIVERUSERID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					query, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else if (pagination) {
				query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
			}

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(receiverUserId);

				if (!pagination) {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end, false);

					Collections.sort(list);

					list = Collections.unmodifiableList(list);
				}
				else {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end);
				}

				cacheResult(list);

				if (useFinderCache) {
					FinderCacheUtil.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception e) {
				if (useFinderCache) {
					FinderCacheUtil.removeResult(finderPath, finderArgs);
				}

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first social activity in the ordered set where receiverUserId = &#63;.
	 *
	 * @param receiverUserId the receiver user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByReceiverUserId_First(
			long receiverUserId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByReceiverUserId_First(
			receiverUserId, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(4);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("receiverUserId=");
		msg.append(receiverUserId);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the first social activity in the ordered set where receiverUserId = &#63;.
	 *
	 * @param receiverUserId the receiver user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByReceiverUserId_First(
		long receiverUserId,
		OrderByComparator<SocialActivity> orderByComparator) {

		List<SocialActivity> list = findByReceiverUserId(
			receiverUserId, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last social activity in the ordered set where receiverUserId = &#63;.
	 *
	 * @param receiverUserId the receiver user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByReceiverUserId_Last(
			long receiverUserId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByReceiverUserId_Last(
			receiverUserId, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(4);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("receiverUserId=");
		msg.append(receiverUserId);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the last social activity in the ordered set where receiverUserId = &#63;.
	 *
	 * @param receiverUserId the receiver user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByReceiverUserId_Last(
		long receiverUserId,
		OrderByComparator<SocialActivity> orderByComparator) {

		int count = countByReceiverUserId(receiverUserId);

		if (count == 0) {
			return null;
		}

		List<SocialActivity> list = findByReceiverUserId(
			receiverUserId, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the social activities before and after the current social activity in the ordered set where receiverUserId = &#63;.
	 *
	 * @param activityId the primary key of the current social activity
	 * @param receiverUserId the receiver user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next social activity
	 * @throws NoSuchActivityException if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity[] findByReceiverUserId_PrevAndNext(
			long activityId, long receiverUserId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = findByPrimaryKey(activityId);

		Session session = null;

		try {
			session = openSession();

			SocialActivity[] array = new SocialActivityImpl[3];

			array[0] = getByReceiverUserId_PrevAndNext(
				session, socialActivity, receiverUserId, orderByComparator,
				true);

			array[1] = socialActivity;

			array[2] = getByReceiverUserId_PrevAndNext(
				session, socialActivity, receiverUserId, orderByComparator,
				false);

			return array;
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	protected SocialActivity getByReceiverUserId_PrevAndNext(
		Session session, SocialActivity socialActivity, long receiverUserId,
		OrderByComparator<SocialActivity> orderByComparator, boolean previous) {

		StringBundler query = null;

		if (orderByComparator != null) {
			query = new StringBundler(
				4 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			query = new StringBundler(3);
		}

		query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

		query.append(_FINDER_COLUMN_RECEIVERUSERID_RECEIVERUSERID_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				query.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						query.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN);
					}
					else {
						query.append(WHERE_LESSER_THAN);
					}
				}
			}

			query.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						query.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC);
					}
					else {
						query.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
		}

		String sql = query.toString();

		Query q = session.createQuery(sql);

		q.setFirstResult(0);
		q.setMaxResults(2);

		QueryPos qPos = QueryPos.getInstance(q);

		qPos.add(receiverUserId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						socialActivity)) {

				qPos.add(orderByConditionValue);
			}
		}

		List<SocialActivity> list = q.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the social activities where receiverUserId = &#63; from the database.
	 *
	 * @param receiverUserId the receiver user ID
	 */
	@Override
	public void removeByReceiverUserId(long receiverUserId) {
		for (SocialActivity socialActivity :
				findByReceiverUserId(
					receiverUserId, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					null)) {

			remove(socialActivity);
		}
	}

	/**
	 * Returns the number of social activities where receiverUserId = &#63;.
	 *
	 * @param receiverUserId the receiver user ID
	 * @return the number of matching social activities
	 */
	@Override
	public int countByReceiverUserId(long receiverUserId) {
		FinderPath finderPath = _finderPathCountByReceiverUserId;

		Object[] finderArgs = new Object[] {receiverUserId};

		Long count = (Long)FinderCacheUtil.getResult(
			finderPath, finderArgs, this);

		if (count == null) {
			StringBundler query = new StringBundler(2);

			query.append(_SQL_COUNT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_RECEIVERUSERID_RECEIVERUSERID_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(receiverUserId);

				count = (Long)q.uniqueResult();

				FinderCacheUtil.putResult(finderPath, finderArgs, count);
			}
			catch (Exception e) {
				FinderCacheUtil.removeResult(finderPath, finderArgs);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_RECEIVERUSERID_RECEIVERUSERID_2 =
		"socialActivity.receiverUserId = ?";

	private FinderPath _finderPathWithPaginationFindByC_C;
	private FinderPath _finderPathWithoutPaginationFindByC_C;
	private FinderPath _finderPathCountByC_C;

	/**
	 * Returns all the social activities where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching social activities
	 */
	@Override
	public List<SocialActivity> findByC_C(long classNameId, long classPK) {
		return findByC_C(
			classNameId, classPK, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the social activities where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @return the range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByC_C(
		long classNameId, long classPK, int start, int end) {

		return findByC_C(classNameId, classPK, start, end, null);
	}

	/**
	 * Returns an ordered range of all the social activities where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByC_C(
		long classNameId, long classPK, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator) {

		return findByC_C(
			classNameId, classPK, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the social activities where classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByC_C(
		long classNameId, long classPK, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator,
		boolean useFinderCache) {

		boolean pagination = true;
		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			pagination = false;

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByC_C;
				finderArgs = new Object[] {classNameId, classPK};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByC_C;
			finderArgs = new Object[] {
				classNameId, classPK, start, end, orderByComparator
			};
		}

		List<SocialActivity> list = null;

		if (useFinderCache) {
			list = (List<SocialActivity>)FinderCacheUtil.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (SocialActivity socialActivity : list) {
					if ((classNameId != socialActivity.getClassNameId()) ||
						(classPK != socialActivity.getClassPK())) {

						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler query = null;

			if (orderByComparator != null) {
				query = new StringBundler(
					4 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				query = new StringBundler(4);
			}

			query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_C_C_CLASSNAMEID_2);

			query.append(_FINDER_COLUMN_C_C_CLASSPK_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					query, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else if (pagination) {
				query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
			}

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(classNameId);

				qPos.add(classPK);

				if (!pagination) {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end, false);

					Collections.sort(list);

					list = Collections.unmodifiableList(list);
				}
				else {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end);
				}

				cacheResult(list);

				if (useFinderCache) {
					FinderCacheUtil.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception e) {
				if (useFinderCache) {
					FinderCacheUtil.removeResult(finderPath, finderArgs);
				}

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first social activity in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByC_C_First(
			long classNameId, long classPK,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByC_C_First(
			classNameId, classPK, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(6);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("classNameId=");
		msg.append(classNameId);

		msg.append(", classPK=");
		msg.append(classPK);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the first social activity in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByC_C_First(
		long classNameId, long classPK,
		OrderByComparator<SocialActivity> orderByComparator) {

		List<SocialActivity> list = findByC_C(
			classNameId, classPK, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last social activity in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByC_C_Last(
			long classNameId, long classPK,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByC_C_Last(
			classNameId, classPK, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(6);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("classNameId=");
		msg.append(classNameId);

		msg.append(", classPK=");
		msg.append(classPK);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the last social activity in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByC_C_Last(
		long classNameId, long classPK,
		OrderByComparator<SocialActivity> orderByComparator) {

		int count = countByC_C(classNameId, classPK);

		if (count == 0) {
			return null;
		}

		List<SocialActivity> list = findByC_C(
			classNameId, classPK, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the social activities before and after the current social activity in the ordered set where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param activityId the primary key of the current social activity
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next social activity
	 * @throws NoSuchActivityException if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity[] findByC_C_PrevAndNext(
			long activityId, long classNameId, long classPK,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = findByPrimaryKey(activityId);

		Session session = null;

		try {
			session = openSession();

			SocialActivity[] array = new SocialActivityImpl[3];

			array[0] = getByC_C_PrevAndNext(
				session, socialActivity, classNameId, classPK,
				orderByComparator, true);

			array[1] = socialActivity;

			array[2] = getByC_C_PrevAndNext(
				session, socialActivity, classNameId, classPK,
				orderByComparator, false);

			return array;
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	protected SocialActivity getByC_C_PrevAndNext(
		Session session, SocialActivity socialActivity, long classNameId,
		long classPK, OrderByComparator<SocialActivity> orderByComparator,
		boolean previous) {

		StringBundler query = null;

		if (orderByComparator != null) {
			query = new StringBundler(
				5 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			query = new StringBundler(4);
		}

		query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

		query.append(_FINDER_COLUMN_C_C_CLASSNAMEID_2);

		query.append(_FINDER_COLUMN_C_C_CLASSPK_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				query.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						query.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN);
					}
					else {
						query.append(WHERE_LESSER_THAN);
					}
				}
			}

			query.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						query.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC);
					}
					else {
						query.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
		}

		String sql = query.toString();

		Query q = session.createQuery(sql);

		q.setFirstResult(0);
		q.setMaxResults(2);

		QueryPos qPos = QueryPos.getInstance(q);

		qPos.add(classNameId);

		qPos.add(classPK);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						socialActivity)) {

				qPos.add(orderByConditionValue);
			}
		}

		List<SocialActivity> list = q.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the social activities where classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 */
	@Override
	public void removeByC_C(long classNameId, long classPK) {
		for (SocialActivity socialActivity :
				findByC_C(
					classNameId, classPK, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
					null)) {

			remove(socialActivity);
		}
	}

	/**
	 * Returns the number of social activities where classNameId = &#63; and classPK = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching social activities
	 */
	@Override
	public int countByC_C(long classNameId, long classPK) {
		FinderPath finderPath = _finderPathCountByC_C;

		Object[] finderArgs = new Object[] {classNameId, classPK};

		Long count = (Long)FinderCacheUtil.getResult(
			finderPath, finderArgs, this);

		if (count == null) {
			StringBundler query = new StringBundler(3);

			query.append(_SQL_COUNT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_C_C_CLASSNAMEID_2);

			query.append(_FINDER_COLUMN_C_C_CLASSPK_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(classNameId);

				qPos.add(classPK);

				count = (Long)q.uniqueResult();

				FinderCacheUtil.putResult(finderPath, finderArgs, count);
			}
			catch (Exception e) {
				FinderCacheUtil.removeResult(finderPath, finderArgs);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_C_C_CLASSNAMEID_2 =
		"socialActivity.classNameId = ? AND ";

	private static final String _FINDER_COLUMN_C_C_CLASSPK_2 =
		"socialActivity.classPK = ?";

	private FinderPath _finderPathWithPaginationFindByM_C_C;
	private FinderPath _finderPathWithoutPaginationFindByM_C_C;
	private FinderPath _finderPathCountByM_C_C;

	/**
	 * Returns all the social activities where mirrorActivityId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the matching social activities
	 */
	@Override
	public List<SocialActivity> findByM_C_C(
		long mirrorActivityId, long classNameId, long classPK) {

		return findByM_C_C(
			mirrorActivityId, classNameId, classPK, QueryUtil.ALL_POS,
			QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the social activities where mirrorActivityId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @return the range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByM_C_C(
		long mirrorActivityId, long classNameId, long classPK, int start,
		int end) {

		return findByM_C_C(
			mirrorActivityId, classNameId, classPK, start, end, null);
	}

	/**
	 * Returns an ordered range of all the social activities where mirrorActivityId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByM_C_C(
		long mirrorActivityId, long classNameId, long classPK, int start,
		int end, OrderByComparator<SocialActivity> orderByComparator) {

		return findByM_C_C(
			mirrorActivityId, classNameId, classPK, start, end,
			orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the social activities where mirrorActivityId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByM_C_C(
		long mirrorActivityId, long classNameId, long classPK, int start,
		int end, OrderByComparator<SocialActivity> orderByComparator,
		boolean useFinderCache) {

		boolean pagination = true;
		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			pagination = false;

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByM_C_C;
				finderArgs = new Object[] {
					mirrorActivityId, classNameId, classPK
				};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByM_C_C;
			finderArgs = new Object[] {
				mirrorActivityId, classNameId, classPK, start, end,
				orderByComparator
			};
		}

		List<SocialActivity> list = null;

		if (useFinderCache) {
			list = (List<SocialActivity>)FinderCacheUtil.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (SocialActivity socialActivity : list) {
					if ((mirrorActivityId !=
							socialActivity.getMirrorActivityId()) ||
						(classNameId != socialActivity.getClassNameId()) ||
						(classPK != socialActivity.getClassPK())) {

						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler query = null;

			if (orderByComparator != null) {
				query = new StringBundler(
					5 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				query = new StringBundler(5);
			}

			query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_M_C_C_MIRRORACTIVITYID_2);

			query.append(_FINDER_COLUMN_M_C_C_CLASSNAMEID_2);

			query.append(_FINDER_COLUMN_M_C_C_CLASSPK_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					query, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else if (pagination) {
				query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
			}

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(mirrorActivityId);

				qPos.add(classNameId);

				qPos.add(classPK);

				if (!pagination) {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end, false);

					Collections.sort(list);

					list = Collections.unmodifiableList(list);
				}
				else {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end);
				}

				cacheResult(list);

				if (useFinderCache) {
					FinderCacheUtil.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception e) {
				if (useFinderCache) {
					FinderCacheUtil.removeResult(finderPath, finderArgs);
				}

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first social activity in the ordered set where mirrorActivityId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByM_C_C_First(
			long mirrorActivityId, long classNameId, long classPK,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByM_C_C_First(
			mirrorActivityId, classNameId, classPK, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(8);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("mirrorActivityId=");
		msg.append(mirrorActivityId);

		msg.append(", classNameId=");
		msg.append(classNameId);

		msg.append(", classPK=");
		msg.append(classPK);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the first social activity in the ordered set where mirrorActivityId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByM_C_C_First(
		long mirrorActivityId, long classNameId, long classPK,
		OrderByComparator<SocialActivity> orderByComparator) {

		List<SocialActivity> list = findByM_C_C(
			mirrorActivityId, classNameId, classPK, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last social activity in the ordered set where mirrorActivityId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByM_C_C_Last(
			long mirrorActivityId, long classNameId, long classPK,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByM_C_C_Last(
			mirrorActivityId, classNameId, classPK, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(8);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("mirrorActivityId=");
		msg.append(mirrorActivityId);

		msg.append(", classNameId=");
		msg.append(classNameId);

		msg.append(", classPK=");
		msg.append(classPK);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the last social activity in the ordered set where mirrorActivityId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByM_C_C_Last(
		long mirrorActivityId, long classNameId, long classPK,
		OrderByComparator<SocialActivity> orderByComparator) {

		int count = countByM_C_C(mirrorActivityId, classNameId, classPK);

		if (count == 0) {
			return null;
		}

		List<SocialActivity> list = findByM_C_C(
			mirrorActivityId, classNameId, classPK, count - 1, count,
			orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the social activities before and after the current social activity in the ordered set where mirrorActivityId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * @param activityId the primary key of the current social activity
	 * @param mirrorActivityId the mirror activity ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next social activity
	 * @throws NoSuchActivityException if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity[] findByM_C_C_PrevAndNext(
			long activityId, long mirrorActivityId, long classNameId,
			long classPK, OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = findByPrimaryKey(activityId);

		Session session = null;

		try {
			session = openSession();

			SocialActivity[] array = new SocialActivityImpl[3];

			array[0] = getByM_C_C_PrevAndNext(
				session, socialActivity, mirrorActivityId, classNameId, classPK,
				orderByComparator, true);

			array[1] = socialActivity;

			array[2] = getByM_C_C_PrevAndNext(
				session, socialActivity, mirrorActivityId, classNameId, classPK,
				orderByComparator, false);

			return array;
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	protected SocialActivity getByM_C_C_PrevAndNext(
		Session session, SocialActivity socialActivity, long mirrorActivityId,
		long classNameId, long classPK,
		OrderByComparator<SocialActivity> orderByComparator, boolean previous) {

		StringBundler query = null;

		if (orderByComparator != null) {
			query = new StringBundler(
				6 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			query = new StringBundler(5);
		}

		query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

		query.append(_FINDER_COLUMN_M_C_C_MIRRORACTIVITYID_2);

		query.append(_FINDER_COLUMN_M_C_C_CLASSNAMEID_2);

		query.append(_FINDER_COLUMN_M_C_C_CLASSPK_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				query.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						query.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN);
					}
					else {
						query.append(WHERE_LESSER_THAN);
					}
				}
			}

			query.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						query.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC);
					}
					else {
						query.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
		}

		String sql = query.toString();

		Query q = session.createQuery(sql);

		q.setFirstResult(0);
		q.setMaxResults(2);

		QueryPos qPos = QueryPos.getInstance(q);

		qPos.add(mirrorActivityId);

		qPos.add(classNameId);

		qPos.add(classPK);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						socialActivity)) {

				qPos.add(orderByConditionValue);
			}
		}

		List<SocialActivity> list = q.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the social activities where mirrorActivityId = &#63; and classNameId = &#63; and classPK = &#63; from the database.
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 */
	@Override
	public void removeByM_C_C(
		long mirrorActivityId, long classNameId, long classPK) {

		for (SocialActivity socialActivity :
				findByM_C_C(
					mirrorActivityId, classNameId, classPK, QueryUtil.ALL_POS,
					QueryUtil.ALL_POS, null)) {

			remove(socialActivity);
		}
	}

	/**
	 * Returns the number of social activities where mirrorActivityId = &#63; and classNameId = &#63; and classPK = &#63;.
	 *
	 * @param mirrorActivityId the mirror activity ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @return the number of matching social activities
	 */
	@Override
	public int countByM_C_C(
		long mirrorActivityId, long classNameId, long classPK) {

		FinderPath finderPath = _finderPathCountByM_C_C;

		Object[] finderArgs = new Object[] {
			mirrorActivityId, classNameId, classPK
		};

		Long count = (Long)FinderCacheUtil.getResult(
			finderPath, finderArgs, this);

		if (count == null) {
			StringBundler query = new StringBundler(4);

			query.append(_SQL_COUNT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_M_C_C_MIRRORACTIVITYID_2);

			query.append(_FINDER_COLUMN_M_C_C_CLASSNAMEID_2);

			query.append(_FINDER_COLUMN_M_C_C_CLASSPK_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(mirrorActivityId);

				qPos.add(classNameId);

				qPos.add(classPK);

				count = (Long)q.uniqueResult();

				FinderCacheUtil.putResult(finderPath, finderArgs, count);
			}
			catch (Exception e) {
				FinderCacheUtil.removeResult(finderPath, finderArgs);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_M_C_C_MIRRORACTIVITYID_2 =
		"socialActivity.mirrorActivityId = ? AND ";

	private static final String _FINDER_COLUMN_M_C_C_CLASSNAMEID_2 =
		"socialActivity.classNameId = ? AND ";

	private static final String _FINDER_COLUMN_M_C_C_CLASSPK_2 =
		"socialActivity.classPK = ?";

	private FinderPath _finderPathWithPaginationFindByC_C_T;
	private FinderPath _finderPathWithoutPaginationFindByC_C_T;
	private FinderPath _finderPathCountByC_C_T;

	/**
	 * Returns all the social activities where classNameId = &#63; and classPK = &#63; and type = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @return the matching social activities
	 */
	@Override
	public List<SocialActivity> findByC_C_T(
		long classNameId, long classPK, int type) {

		return findByC_C_T(
			classNameId, classPK, type, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
			null);
	}

	/**
	 * Returns a range of all the social activities where classNameId = &#63; and classPK = &#63; and type = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @return the range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByC_C_T(
		long classNameId, long classPK, int type, int start, int end) {

		return findByC_C_T(classNameId, classPK, type, start, end, null);
	}

	/**
	 * Returns an ordered range of all the social activities where classNameId = &#63; and classPK = &#63; and type = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByC_C_T(
		long classNameId, long classPK, int type, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator) {

		return findByC_C_T(
			classNameId, classPK, type, start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the social activities where classNameId = &#63; and classPK = &#63; and type = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByC_C_T(
		long classNameId, long classPK, int type, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator,
		boolean useFinderCache) {

		boolean pagination = true;
		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			pagination = false;

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByC_C_T;
				finderArgs = new Object[] {classNameId, classPK, type};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByC_C_T;
			finderArgs = new Object[] {
				classNameId, classPK, type, start, end, orderByComparator
			};
		}

		List<SocialActivity> list = null;

		if (useFinderCache) {
			list = (List<SocialActivity>)FinderCacheUtil.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (SocialActivity socialActivity : list) {
					if ((classNameId != socialActivity.getClassNameId()) ||
						(classPK != socialActivity.getClassPK()) ||
						(type != socialActivity.getType())) {

						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler query = null;

			if (orderByComparator != null) {
				query = new StringBundler(
					5 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				query = new StringBundler(5);
			}

			query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_C_C_T_CLASSNAMEID_2);

			query.append(_FINDER_COLUMN_C_C_T_CLASSPK_2);

			query.append(_FINDER_COLUMN_C_C_T_TYPE_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					query, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else if (pagination) {
				query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
			}

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(classNameId);

				qPos.add(classPK);

				qPos.add(type);

				if (!pagination) {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end, false);

					Collections.sort(list);

					list = Collections.unmodifiableList(list);
				}
				else {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end);
				}

				cacheResult(list);

				if (useFinderCache) {
					FinderCacheUtil.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception e) {
				if (useFinderCache) {
					FinderCacheUtil.removeResult(finderPath, finderArgs);
				}

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first social activity in the ordered set where classNameId = &#63; and classPK = &#63; and type = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByC_C_T_First(
			long classNameId, long classPK, int type,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByC_C_T_First(
			classNameId, classPK, type, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(8);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("classNameId=");
		msg.append(classNameId);

		msg.append(", classPK=");
		msg.append(classPK);

		msg.append(", type=");
		msg.append(type);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the first social activity in the ordered set where classNameId = &#63; and classPK = &#63; and type = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByC_C_T_First(
		long classNameId, long classPK, int type,
		OrderByComparator<SocialActivity> orderByComparator) {

		List<SocialActivity> list = findByC_C_T(
			classNameId, classPK, type, 0, 1, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last social activity in the ordered set where classNameId = &#63; and classPK = &#63; and type = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByC_C_T_Last(
			long classNameId, long classPK, int type,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByC_C_T_Last(
			classNameId, classPK, type, orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(8);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("classNameId=");
		msg.append(classNameId);

		msg.append(", classPK=");
		msg.append(classPK);

		msg.append(", type=");
		msg.append(type);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the last social activity in the ordered set where classNameId = &#63; and classPK = &#63; and type = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByC_C_T_Last(
		long classNameId, long classPK, int type,
		OrderByComparator<SocialActivity> orderByComparator) {

		int count = countByC_C_T(classNameId, classPK, type);

		if (count == 0) {
			return null;
		}

		List<SocialActivity> list = findByC_C_T(
			classNameId, classPK, type, count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the social activities before and after the current social activity in the ordered set where classNameId = &#63; and classPK = &#63; and type = &#63;.
	 *
	 * @param activityId the primary key of the current social activity
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next social activity
	 * @throws NoSuchActivityException if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity[] findByC_C_T_PrevAndNext(
			long activityId, long classNameId, long classPK, int type,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = findByPrimaryKey(activityId);

		Session session = null;

		try {
			session = openSession();

			SocialActivity[] array = new SocialActivityImpl[3];

			array[0] = getByC_C_T_PrevAndNext(
				session, socialActivity, classNameId, classPK, type,
				orderByComparator, true);

			array[1] = socialActivity;

			array[2] = getByC_C_T_PrevAndNext(
				session, socialActivity, classNameId, classPK, type,
				orderByComparator, false);

			return array;
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	protected SocialActivity getByC_C_T_PrevAndNext(
		Session session, SocialActivity socialActivity, long classNameId,
		long classPK, int type,
		OrderByComparator<SocialActivity> orderByComparator, boolean previous) {

		StringBundler query = null;

		if (orderByComparator != null) {
			query = new StringBundler(
				6 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			query = new StringBundler(5);
		}

		query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

		query.append(_FINDER_COLUMN_C_C_T_CLASSNAMEID_2);

		query.append(_FINDER_COLUMN_C_C_T_CLASSPK_2);

		query.append(_FINDER_COLUMN_C_C_T_TYPE_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				query.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						query.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN);
					}
					else {
						query.append(WHERE_LESSER_THAN);
					}
				}
			}

			query.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						query.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC);
					}
					else {
						query.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
		}

		String sql = query.toString();

		Query q = session.createQuery(sql);

		q.setFirstResult(0);
		q.setMaxResults(2);

		QueryPos qPos = QueryPos.getInstance(q);

		qPos.add(classNameId);

		qPos.add(classPK);

		qPos.add(type);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						socialActivity)) {

				qPos.add(orderByConditionValue);
			}
		}

		List<SocialActivity> list = q.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the social activities where classNameId = &#63; and classPK = &#63; and type = &#63; from the database.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 */
	@Override
	public void removeByC_C_T(long classNameId, long classPK, int type) {
		for (SocialActivity socialActivity :
				findByC_C_T(
					classNameId, classPK, type, QueryUtil.ALL_POS,
					QueryUtil.ALL_POS, null)) {

			remove(socialActivity);
		}
	}

	/**
	 * Returns the number of social activities where classNameId = &#63; and classPK = &#63; and type = &#63;.
	 *
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @return the number of matching social activities
	 */
	@Override
	public int countByC_C_T(long classNameId, long classPK, int type) {
		FinderPath finderPath = _finderPathCountByC_C_T;

		Object[] finderArgs = new Object[] {classNameId, classPK, type};

		Long count = (Long)FinderCacheUtil.getResult(
			finderPath, finderArgs, this);

		if (count == null) {
			StringBundler query = new StringBundler(4);

			query.append(_SQL_COUNT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_C_C_T_CLASSNAMEID_2);

			query.append(_FINDER_COLUMN_C_C_T_CLASSPK_2);

			query.append(_FINDER_COLUMN_C_C_T_TYPE_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(classNameId);

				qPos.add(classPK);

				qPos.add(type);

				count = (Long)q.uniqueResult();

				FinderCacheUtil.putResult(finderPath, finderArgs, count);
			}
			catch (Exception e) {
				FinderCacheUtil.removeResult(finderPath, finderArgs);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_C_C_T_CLASSNAMEID_2 =
		"socialActivity.classNameId = ? AND ";

	private static final String _FINDER_COLUMN_C_C_T_CLASSPK_2 =
		"socialActivity.classPK = ? AND ";

	private static final String _FINDER_COLUMN_C_C_T_TYPE_2 =
		"socialActivity.type = ?";

	private FinderPath _finderPathWithPaginationFindByG_U_C_C_T_R;
	private FinderPath _finderPathWithoutPaginationFindByG_U_C_C_T_R;
	private FinderPath _finderPathCountByG_U_C_C_T_R;

	/**
	 * Returns all the social activities where groupId = &#63; and userId = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @return the matching social activities
	 */
	@Override
	public List<SocialActivity> findByG_U_C_C_T_R(
		long groupId, long userId, long classNameId, long classPK, int type,
		long receiverUserId) {

		return findByG_U_C_C_T_R(
			groupId, userId, classNameId, classPK, type, receiverUserId,
			QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the social activities where groupId = &#63; and userId = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @return the range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByG_U_C_C_T_R(
		long groupId, long userId, long classNameId, long classPK, int type,
		long receiverUserId, int start, int end) {

		return findByG_U_C_C_T_R(
			groupId, userId, classNameId, classPK, type, receiverUserId, start,
			end, null);
	}

	/**
	 * Returns an ordered range of all the social activities where groupId = &#63; and userId = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByG_U_C_C_T_R(
		long groupId, long userId, long classNameId, long classPK, int type,
		long receiverUserId, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator) {

		return findByG_U_C_C_T_R(
			groupId, userId, classNameId, classPK, type, receiverUserId, start,
			end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the social activities where groupId = &#63; and userId = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63;.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of matching social activities
	 */
	@Override
	public List<SocialActivity> findByG_U_C_C_T_R(
		long groupId, long userId, long classNameId, long classPK, int type,
		long receiverUserId, int start, int end,
		OrderByComparator<SocialActivity> orderByComparator,
		boolean useFinderCache) {

		boolean pagination = true;
		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			pagination = false;

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindByG_U_C_C_T_R;
				finderArgs = new Object[] {
					groupId, userId, classNameId, classPK, type, receiverUserId
				};
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindByG_U_C_C_T_R;
			finderArgs = new Object[] {
				groupId, userId, classNameId, classPK, type, receiverUserId,
				start, end, orderByComparator
			};
		}

		List<SocialActivity> list = null;

		if (useFinderCache) {
			list = (List<SocialActivity>)FinderCacheUtil.getResult(
				finderPath, finderArgs, this);

			if ((list != null) && !list.isEmpty()) {
				for (SocialActivity socialActivity : list) {
					if ((groupId != socialActivity.getGroupId()) ||
						(userId != socialActivity.getUserId()) ||
						(classNameId != socialActivity.getClassNameId()) ||
						(classPK != socialActivity.getClassPK()) ||
						(type != socialActivity.getType()) ||
						(receiverUserId !=
							socialActivity.getReceiverUserId())) {

						list = null;

						break;
					}
				}
			}
		}

		if (list == null) {
			StringBundler query = null;

			if (orderByComparator != null) {
				query = new StringBundler(
					8 + (orderByComparator.getOrderByFields().length * 2));
			}
			else {
				query = new StringBundler(8);
			}

			query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_G_U_C_C_T_R_GROUPID_2);

			query.append(_FINDER_COLUMN_G_U_C_C_T_R_USERID_2);

			query.append(_FINDER_COLUMN_G_U_C_C_T_R_CLASSNAMEID_2);

			query.append(_FINDER_COLUMN_G_U_C_C_T_R_CLASSPK_2);

			query.append(_FINDER_COLUMN_G_U_C_C_T_R_TYPE_2);

			query.append(_FINDER_COLUMN_G_U_C_C_T_R_RECEIVERUSERID_2);

			if (orderByComparator != null) {
				appendOrderByComparator(
					query, _ORDER_BY_ENTITY_ALIAS, orderByComparator);
			}
			else if (pagination) {
				query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
			}

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(groupId);

				qPos.add(userId);

				qPos.add(classNameId);

				qPos.add(classPK);

				qPos.add(type);

				qPos.add(receiverUserId);

				if (!pagination) {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end, false);

					Collections.sort(list);

					list = Collections.unmodifiableList(list);
				}
				else {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end);
				}

				cacheResult(list);

				if (useFinderCache) {
					FinderCacheUtil.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception e) {
				if (useFinderCache) {
					FinderCacheUtil.removeResult(finderPath, finderArgs);
				}

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Returns the first social activity in the ordered set where groupId = &#63; and userId = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByG_U_C_C_T_R_First(
			long groupId, long userId, long classNameId, long classPK, int type,
			long receiverUserId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByG_U_C_C_T_R_First(
			groupId, userId, classNameId, classPK, type, receiverUserId,
			orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(14);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("groupId=");
		msg.append(groupId);

		msg.append(", userId=");
		msg.append(userId);

		msg.append(", classNameId=");
		msg.append(classNameId);

		msg.append(", classPK=");
		msg.append(classPK);

		msg.append(", type=");
		msg.append(type);

		msg.append(", receiverUserId=");
		msg.append(receiverUserId);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the first social activity in the ordered set where groupId = &#63; and userId = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the first matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByG_U_C_C_T_R_First(
		long groupId, long userId, long classNameId, long classPK, int type,
		long receiverUserId,
		OrderByComparator<SocialActivity> orderByComparator) {

		List<SocialActivity> list = findByG_U_C_C_T_R(
			groupId, userId, classNameId, classPK, type, receiverUserId, 0, 1,
			orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the last social activity in the ordered set where groupId = &#63; and userId = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByG_U_C_C_T_R_Last(
			long groupId, long userId, long classNameId, long classPK, int type,
			long receiverUserId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByG_U_C_C_T_R_Last(
			groupId, userId, classNameId, classPK, type, receiverUserId,
			orderByComparator);

		if (socialActivity != null) {
			return socialActivity;
		}

		StringBundler msg = new StringBundler(14);

		msg.append(_NO_SUCH_ENTITY_WITH_KEY);

		msg.append("groupId=");
		msg.append(groupId);

		msg.append(", userId=");
		msg.append(userId);

		msg.append(", classNameId=");
		msg.append(classNameId);

		msg.append(", classPK=");
		msg.append(classPK);

		msg.append(", type=");
		msg.append(type);

		msg.append(", receiverUserId=");
		msg.append(receiverUserId);

		msg.append("}");

		throw new NoSuchActivityException(msg.toString());
	}

	/**
	 * Returns the last social activity in the ordered set where groupId = &#63; and userId = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the last matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByG_U_C_C_T_R_Last(
		long groupId, long userId, long classNameId, long classPK, int type,
		long receiverUserId,
		OrderByComparator<SocialActivity> orderByComparator) {

		int count = countByG_U_C_C_T_R(
			groupId, userId, classNameId, classPK, type, receiverUserId);

		if (count == 0) {
			return null;
		}

		List<SocialActivity> list = findByG_U_C_C_T_R(
			groupId, userId, classNameId, classPK, type, receiverUserId,
			count - 1, count, orderByComparator);

		if (!list.isEmpty()) {
			return list.get(0);
		}

		return null;
	}

	/**
	 * Returns the social activities before and after the current social activity in the ordered set where groupId = &#63; and userId = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63;.
	 *
	 * @param activityId the primary key of the current social activity
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @param orderByComparator the comparator to order the set by (optionally <code>null</code>)
	 * @return the previous, current, and next social activity
	 * @throws NoSuchActivityException if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity[] findByG_U_C_C_T_R_PrevAndNext(
			long activityId, long groupId, long userId, long classNameId,
			long classPK, int type, long receiverUserId,
			OrderByComparator<SocialActivity> orderByComparator)
		throws NoSuchActivityException {

		SocialActivity socialActivity = findByPrimaryKey(activityId);

		Session session = null;

		try {
			session = openSession();

			SocialActivity[] array = new SocialActivityImpl[3];

			array[0] = getByG_U_C_C_T_R_PrevAndNext(
				session, socialActivity, groupId, userId, classNameId, classPK,
				type, receiverUserId, orderByComparator, true);

			array[1] = socialActivity;

			array[2] = getByG_U_C_C_T_R_PrevAndNext(
				session, socialActivity, groupId, userId, classNameId, classPK,
				type, receiverUserId, orderByComparator, false);

			return array;
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	protected SocialActivity getByG_U_C_C_T_R_PrevAndNext(
		Session session, SocialActivity socialActivity, long groupId,
		long userId, long classNameId, long classPK, int type,
		long receiverUserId,
		OrderByComparator<SocialActivity> orderByComparator, boolean previous) {

		StringBundler query = null;

		if (orderByComparator != null) {
			query = new StringBundler(
				9 + (orderByComparator.getOrderByConditionFields().length * 3) +
					(orderByComparator.getOrderByFields().length * 3));
		}
		else {
			query = new StringBundler(8);
		}

		query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

		query.append(_FINDER_COLUMN_G_U_C_C_T_R_GROUPID_2);

		query.append(_FINDER_COLUMN_G_U_C_C_T_R_USERID_2);

		query.append(_FINDER_COLUMN_G_U_C_C_T_R_CLASSNAMEID_2);

		query.append(_FINDER_COLUMN_G_U_C_C_T_R_CLASSPK_2);

		query.append(_FINDER_COLUMN_G_U_C_C_T_R_TYPE_2);

		query.append(_FINDER_COLUMN_G_U_C_C_T_R_RECEIVERUSERID_2);

		if (orderByComparator != null) {
			String[] orderByConditionFields =
				orderByComparator.getOrderByConditionFields();

			if (orderByConditionFields.length > 0) {
				query.append(WHERE_AND);
			}

			for (int i = 0; i < orderByConditionFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByConditionFields[i]);

				if ((i + 1) < orderByConditionFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN_HAS_NEXT);
					}
					else {
						query.append(WHERE_LESSER_THAN_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(WHERE_GREATER_THAN);
					}
					else {
						query.append(WHERE_LESSER_THAN);
					}
				}
			}

			query.append(ORDER_BY_CLAUSE);

			String[] orderByFields = orderByComparator.getOrderByFields();

			for (int i = 0; i < orderByFields.length; i++) {
				query.append(_ORDER_BY_ENTITY_ALIAS);
				query.append(orderByFields[i]);

				if ((i + 1) < orderByFields.length) {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC_HAS_NEXT);
					}
					else {
						query.append(ORDER_BY_DESC_HAS_NEXT);
					}
				}
				else {
					if (orderByComparator.isAscending() ^ previous) {
						query.append(ORDER_BY_ASC);
					}
					else {
						query.append(ORDER_BY_DESC);
					}
				}
			}
		}
		else {
			query.append(SocialActivityModelImpl.ORDER_BY_JPQL);
		}

		String sql = query.toString();

		Query q = session.createQuery(sql);

		q.setFirstResult(0);
		q.setMaxResults(2);

		QueryPos qPos = QueryPos.getInstance(q);

		qPos.add(groupId);

		qPos.add(userId);

		qPos.add(classNameId);

		qPos.add(classPK);

		qPos.add(type);

		qPos.add(receiverUserId);

		if (orderByComparator != null) {
			for (Object orderByConditionValue :
					orderByComparator.getOrderByConditionValues(
						socialActivity)) {

				qPos.add(orderByConditionValue);
			}
		}

		List<SocialActivity> list = q.list();

		if (list.size() == 2) {
			return list.get(1);
		}
		else {
			return null;
		}
	}

	/**
	 * Removes all the social activities where groupId = &#63; and userId = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63; from the database.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 */
	@Override
	public void removeByG_U_C_C_T_R(
		long groupId, long userId, long classNameId, long classPK, int type,
		long receiverUserId) {

		for (SocialActivity socialActivity :
				findByG_U_C_C_T_R(
					groupId, userId, classNameId, classPK, type, receiverUserId,
					QueryUtil.ALL_POS, QueryUtil.ALL_POS, null)) {

			remove(socialActivity);
		}
	}

	/**
	 * Returns the number of social activities where groupId = &#63; and userId = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @return the number of matching social activities
	 */
	@Override
	public int countByG_U_C_C_T_R(
		long groupId, long userId, long classNameId, long classPK, int type,
		long receiverUserId) {

		FinderPath finderPath = _finderPathCountByG_U_C_C_T_R;

		Object[] finderArgs = new Object[] {
			groupId, userId, classNameId, classPK, type, receiverUserId
		};

		Long count = (Long)FinderCacheUtil.getResult(
			finderPath, finderArgs, this);

		if (count == null) {
			StringBundler query = new StringBundler(7);

			query.append(_SQL_COUNT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_G_U_C_C_T_R_GROUPID_2);

			query.append(_FINDER_COLUMN_G_U_C_C_T_R_USERID_2);

			query.append(_FINDER_COLUMN_G_U_C_C_T_R_CLASSNAMEID_2);

			query.append(_FINDER_COLUMN_G_U_C_C_T_R_CLASSPK_2);

			query.append(_FINDER_COLUMN_G_U_C_C_T_R_TYPE_2);

			query.append(_FINDER_COLUMN_G_U_C_C_T_R_RECEIVERUSERID_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(groupId);

				qPos.add(userId);

				qPos.add(classNameId);

				qPos.add(classPK);

				qPos.add(type);

				qPos.add(receiverUserId);

				count = (Long)q.uniqueResult();

				FinderCacheUtil.putResult(finderPath, finderArgs, count);
			}
			catch (Exception e) {
				FinderCacheUtil.removeResult(finderPath, finderArgs);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_G_U_C_C_T_R_GROUPID_2 =
		"socialActivity.groupId = ? AND ";

	private static final String _FINDER_COLUMN_G_U_C_C_T_R_USERID_2 =
		"socialActivity.userId = ? AND ";

	private static final String _FINDER_COLUMN_G_U_C_C_T_R_CLASSNAMEID_2 =
		"socialActivity.classNameId = ? AND ";

	private static final String _FINDER_COLUMN_G_U_C_C_T_R_CLASSPK_2 =
		"socialActivity.classPK = ? AND ";

	private static final String _FINDER_COLUMN_G_U_C_C_T_R_TYPE_2 =
		"socialActivity.type = ? AND ";

	private static final String _FINDER_COLUMN_G_U_C_C_T_R_RECEIVERUSERID_2 =
		"socialActivity.receiverUserId = ?";

	private FinderPath _finderPathFetchByG_U_CD_C_C_T_R;
	private FinderPath _finderPathCountByG_U_CD_C_C_T_R;

	/**
	 * Returns the social activity where groupId = &#63; and userId = &#63; and createDate = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63; or throws a <code>NoSuchActivityException</code> if it could not be found.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param createDate the create date
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @return the matching social activity
	 * @throws NoSuchActivityException if a matching social activity could not be found
	 */
	@Override
	public SocialActivity findByG_U_CD_C_C_T_R(
			long groupId, long userId, long createDate, long classNameId,
			long classPK, int type, long receiverUserId)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByG_U_CD_C_C_T_R(
			groupId, userId, createDate, classNameId, classPK, type,
			receiverUserId);

		if (socialActivity == null) {
			StringBundler msg = new StringBundler(16);

			msg.append(_NO_SUCH_ENTITY_WITH_KEY);

			msg.append("groupId=");
			msg.append(groupId);

			msg.append(", userId=");
			msg.append(userId);

			msg.append(", createDate=");
			msg.append(createDate);

			msg.append(", classNameId=");
			msg.append(classNameId);

			msg.append(", classPK=");
			msg.append(classPK);

			msg.append(", type=");
			msg.append(type);

			msg.append(", receiverUserId=");
			msg.append(receiverUserId);

			msg.append("}");

			if (_log.isDebugEnabled()) {
				_log.debug(msg.toString());
			}

			throw new NoSuchActivityException(msg.toString());
		}

		return socialActivity;
	}

	/**
	 * Returns the social activity where groupId = &#63; and userId = &#63; and createDate = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63; or returns <code>null</code> if it could not be found. Uses the finder cache.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param createDate the create date
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @return the matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByG_U_CD_C_C_T_R(
		long groupId, long userId, long createDate, long classNameId,
		long classPK, int type, long receiverUserId) {

		return fetchByG_U_CD_C_C_T_R(
			groupId, userId, createDate, classNameId, classPK, type,
			receiverUserId, true);
	}

	/**
	 * Returns the social activity where groupId = &#63; and userId = &#63; and createDate = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63; or returns <code>null</code> if it could not be found, optionally using the finder cache.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param createDate the create date
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @param useFinderCache whether to use the finder cache
	 * @return the matching social activity, or <code>null</code> if a matching social activity could not be found
	 */
	@Override
	public SocialActivity fetchByG_U_CD_C_C_T_R(
		long groupId, long userId, long createDate, long classNameId,
		long classPK, int type, long receiverUserId, boolean useFinderCache) {

		Object[] finderArgs = null;

		if (useFinderCache) {
			finderArgs = new Object[] {
				groupId, userId, createDate, classNameId, classPK, type,
				receiverUserId
			};
		}

		Object result = null;

		if (useFinderCache) {
			result = FinderCacheUtil.getResult(
				_finderPathFetchByG_U_CD_C_C_T_R, finderArgs, this);
		}

		if (result instanceof SocialActivity) {
			SocialActivity socialActivity = (SocialActivity)result;

			if ((groupId != socialActivity.getGroupId()) ||
				(userId != socialActivity.getUserId()) ||
				(createDate != socialActivity.getCreateDate()) ||
				(classNameId != socialActivity.getClassNameId()) ||
				(classPK != socialActivity.getClassPK()) ||
				(type != socialActivity.getType()) ||
				(receiverUserId != socialActivity.getReceiverUserId())) {

				result = null;
			}
		}

		if (result == null) {
			StringBundler query = new StringBundler(9);

			query.append(_SQL_SELECT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_G_U_CD_C_C_T_R_GROUPID_2);

			query.append(_FINDER_COLUMN_G_U_CD_C_C_T_R_USERID_2);

			query.append(_FINDER_COLUMN_G_U_CD_C_C_T_R_CREATEDATE_2);

			query.append(_FINDER_COLUMN_G_U_CD_C_C_T_R_CLASSNAMEID_2);

			query.append(_FINDER_COLUMN_G_U_CD_C_C_T_R_CLASSPK_2);

			query.append(_FINDER_COLUMN_G_U_CD_C_C_T_R_TYPE_2);

			query.append(_FINDER_COLUMN_G_U_CD_C_C_T_R_RECEIVERUSERID_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(groupId);

				qPos.add(userId);

				qPos.add(createDate);

				qPos.add(classNameId);

				qPos.add(classPK);

				qPos.add(type);

				qPos.add(receiverUserId);

				List<SocialActivity> list = q.list();

				if (list.isEmpty()) {
					if (useFinderCache) {
						FinderCacheUtil.putResult(
							_finderPathFetchByG_U_CD_C_C_T_R, finderArgs, list);
					}
				}
				else {
					SocialActivity socialActivity = list.get(0);

					result = socialActivity;

					cacheResult(socialActivity);
				}
			}
			catch (Exception e) {
				if (useFinderCache) {
					FinderCacheUtil.removeResult(
						_finderPathFetchByG_U_CD_C_C_T_R, finderArgs);
				}

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		if (result instanceof List<?>) {
			return null;
		}
		else {
			return (SocialActivity)result;
		}
	}

	/**
	 * Removes the social activity where groupId = &#63; and userId = &#63; and createDate = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63; from the database.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param createDate the create date
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @return the social activity that was removed
	 */
	@Override
	public SocialActivity removeByG_U_CD_C_C_T_R(
			long groupId, long userId, long createDate, long classNameId,
			long classPK, int type, long receiverUserId)
		throws NoSuchActivityException {

		SocialActivity socialActivity = findByG_U_CD_C_C_T_R(
			groupId, userId, createDate, classNameId, classPK, type,
			receiverUserId);

		return remove(socialActivity);
	}

	/**
	 * Returns the number of social activities where groupId = &#63; and userId = &#63; and createDate = &#63; and classNameId = &#63; and classPK = &#63; and type = &#63; and receiverUserId = &#63;.
	 *
	 * @param groupId the group ID
	 * @param userId the user ID
	 * @param createDate the create date
	 * @param classNameId the class name ID
	 * @param classPK the class pk
	 * @param type the type
	 * @param receiverUserId the receiver user ID
	 * @return the number of matching social activities
	 */
	@Override
	public int countByG_U_CD_C_C_T_R(
		long groupId, long userId, long createDate, long classNameId,
		long classPK, int type, long receiverUserId) {

		FinderPath finderPath = _finderPathCountByG_U_CD_C_C_T_R;

		Object[] finderArgs = new Object[] {
			groupId, userId, createDate, classNameId, classPK, type,
			receiverUserId
		};

		Long count = (Long)FinderCacheUtil.getResult(
			finderPath, finderArgs, this);

		if (count == null) {
			StringBundler query = new StringBundler(8);

			query.append(_SQL_COUNT_SOCIALACTIVITY_WHERE);

			query.append(_FINDER_COLUMN_G_U_CD_C_C_T_R_GROUPID_2);

			query.append(_FINDER_COLUMN_G_U_CD_C_C_T_R_USERID_2);

			query.append(_FINDER_COLUMN_G_U_CD_C_C_T_R_CREATEDATE_2);

			query.append(_FINDER_COLUMN_G_U_CD_C_C_T_R_CLASSNAMEID_2);

			query.append(_FINDER_COLUMN_G_U_CD_C_C_T_R_CLASSPK_2);

			query.append(_FINDER_COLUMN_G_U_CD_C_C_T_R_TYPE_2);

			query.append(_FINDER_COLUMN_G_U_CD_C_C_T_R_RECEIVERUSERID_2);

			String sql = query.toString();

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				QueryPos qPos = QueryPos.getInstance(q);

				qPos.add(groupId);

				qPos.add(userId);

				qPos.add(createDate);

				qPos.add(classNameId);

				qPos.add(classPK);

				qPos.add(type);

				qPos.add(receiverUserId);

				count = (Long)q.uniqueResult();

				FinderCacheUtil.putResult(finderPath, finderArgs, count);
			}
			catch (Exception e) {
				FinderCacheUtil.removeResult(finderPath, finderArgs);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	private static final String _FINDER_COLUMN_G_U_CD_C_C_T_R_GROUPID_2 =
		"socialActivity.groupId = ? AND ";

	private static final String _FINDER_COLUMN_G_U_CD_C_C_T_R_USERID_2 =
		"socialActivity.userId = ? AND ";

	private static final String _FINDER_COLUMN_G_U_CD_C_C_T_R_CREATEDATE_2 =
		"socialActivity.createDate = ? AND ";

	private static final String _FINDER_COLUMN_G_U_CD_C_C_T_R_CLASSNAMEID_2 =
		"socialActivity.classNameId = ? AND ";

	private static final String _FINDER_COLUMN_G_U_CD_C_C_T_R_CLASSPK_2 =
		"socialActivity.classPK = ? AND ";

	private static final String _FINDER_COLUMN_G_U_CD_C_C_T_R_TYPE_2 =
		"socialActivity.type = ? AND ";

	private static final String _FINDER_COLUMN_G_U_CD_C_C_T_R_RECEIVERUSERID_2 =
		"socialActivity.receiverUserId = ?";

	public SocialActivityPersistenceImpl() {
		setModelClass(SocialActivity.class);

		setModelImplClass(SocialActivityImpl.class);
		setModelPKClass(long.class);
		setEntityCacheEnabled(SocialActivityModelImpl.ENTITY_CACHE_ENABLED);

		Map<String, String> dbColumnNames = new HashMap<String, String>();

		dbColumnNames.put("type", "type_");

		setDBColumnNames(dbColumnNames);
	}

	/**
	 * Caches the social activity in the entity cache if it is enabled.
	 *
	 * @param socialActivity the social activity
	 */
	@Override
	public void cacheResult(SocialActivity socialActivity) {
		EntityCacheUtil.putResult(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityImpl.class, socialActivity.getPrimaryKey(),
			socialActivity);

		FinderCacheUtil.putResult(
			_finderPathFetchByMirrorActivityId,
			new Object[] {socialActivity.getMirrorActivityId()},
			socialActivity);

		FinderCacheUtil.putResult(
			_finderPathFetchByG_U_CD_C_C_T_R,
			new Object[] {
				socialActivity.getGroupId(), socialActivity.getUserId(),
				socialActivity.getCreateDate(), socialActivity.getClassNameId(),
				socialActivity.getClassPK(), socialActivity.getType(),
				socialActivity.getReceiverUserId()
			},
			socialActivity);

		socialActivity.resetOriginalValues();
	}

	/**
	 * Caches the social activities in the entity cache if it is enabled.
	 *
	 * @param socialActivities the social activities
	 */
	@Override
	public void cacheResult(List<SocialActivity> socialActivities) {
		for (SocialActivity socialActivity : socialActivities) {
			if (EntityCacheUtil.getResult(
					SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
					SocialActivityImpl.class, socialActivity.getPrimaryKey()) ==
						null) {

				cacheResult(socialActivity);
			}
			else {
				socialActivity.resetOriginalValues();
			}
		}
	}

	/**
	 * Clears the cache for all social activities.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>com.liferay.portal.kernel.dao.orm.FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache() {
		EntityCacheUtil.clearCache(SocialActivityImpl.class);

		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_ENTITY);
		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);
	}

	/**
	 * Clears the cache for the social activity.
	 *
	 * <p>
	 * The <code>EntityCache</code> and <code>com.liferay.portal.kernel.dao.orm.FinderCache</code> are both cleared by this method.
	 * </p>
	 */
	@Override
	public void clearCache(SocialActivity socialActivity) {
		EntityCacheUtil.removeResult(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityImpl.class, socialActivity.getPrimaryKey());

		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);

		clearUniqueFindersCache((SocialActivityModelImpl)socialActivity, true);
	}

	@Override
	public void clearCache(List<SocialActivity> socialActivities) {
		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);

		for (SocialActivity socialActivity : socialActivities) {
			EntityCacheUtil.removeResult(
				SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
				SocialActivityImpl.class, socialActivity.getPrimaryKey());

			clearUniqueFindersCache(
				(SocialActivityModelImpl)socialActivity, true);
		}
	}

	protected void cacheUniqueFindersCache(
		SocialActivityModelImpl socialActivityModelImpl) {

		Object[] args = new Object[] {
			socialActivityModelImpl.getMirrorActivityId()
		};

		FinderCacheUtil.putResult(
			_finderPathCountByMirrorActivityId, args, Long.valueOf(1), false);
		FinderCacheUtil.putResult(
			_finderPathFetchByMirrorActivityId, args, socialActivityModelImpl,
			false);

		args = new Object[] {
			socialActivityModelImpl.getGroupId(),
			socialActivityModelImpl.getUserId(),
			socialActivityModelImpl.getCreateDate(),
			socialActivityModelImpl.getClassNameId(),
			socialActivityModelImpl.getClassPK(),
			socialActivityModelImpl.getType(),
			socialActivityModelImpl.getReceiverUserId()
		};

		FinderCacheUtil.putResult(
			_finderPathCountByG_U_CD_C_C_T_R, args, Long.valueOf(1), false);
		FinderCacheUtil.putResult(
			_finderPathFetchByG_U_CD_C_C_T_R, args, socialActivityModelImpl,
			false);
	}

	protected void clearUniqueFindersCache(
		SocialActivityModelImpl socialActivityModelImpl, boolean clearCurrent) {

		if (clearCurrent) {
			Object[] args = new Object[] {
				socialActivityModelImpl.getMirrorActivityId()
			};

			FinderCacheUtil.removeResult(
				_finderPathCountByMirrorActivityId, args);
			FinderCacheUtil.removeResult(
				_finderPathFetchByMirrorActivityId, args);
		}

		if ((socialActivityModelImpl.getColumnBitmask() &
			 _finderPathFetchByMirrorActivityId.getColumnBitmask()) != 0) {

			Object[] args = new Object[] {
				socialActivityModelImpl.getOriginalMirrorActivityId()
			};

			FinderCacheUtil.removeResult(
				_finderPathCountByMirrorActivityId, args);
			FinderCacheUtil.removeResult(
				_finderPathFetchByMirrorActivityId, args);
		}

		if (clearCurrent) {
			Object[] args = new Object[] {
				socialActivityModelImpl.getGroupId(),
				socialActivityModelImpl.getUserId(),
				socialActivityModelImpl.getCreateDate(),
				socialActivityModelImpl.getClassNameId(),
				socialActivityModelImpl.getClassPK(),
				socialActivityModelImpl.getType(),
				socialActivityModelImpl.getReceiverUserId()
			};

			FinderCacheUtil.removeResult(
				_finderPathCountByG_U_CD_C_C_T_R, args);
			FinderCacheUtil.removeResult(
				_finderPathFetchByG_U_CD_C_C_T_R, args);
		}

		if ((socialActivityModelImpl.getColumnBitmask() &
			 _finderPathFetchByG_U_CD_C_C_T_R.getColumnBitmask()) != 0) {

			Object[] args = new Object[] {
				socialActivityModelImpl.getOriginalGroupId(),
				socialActivityModelImpl.getOriginalUserId(),
				socialActivityModelImpl.getOriginalCreateDate(),
				socialActivityModelImpl.getOriginalClassNameId(),
				socialActivityModelImpl.getOriginalClassPK(),
				socialActivityModelImpl.getOriginalType(),
				socialActivityModelImpl.getOriginalReceiverUserId()
			};

			FinderCacheUtil.removeResult(
				_finderPathCountByG_U_CD_C_C_T_R, args);
			FinderCacheUtil.removeResult(
				_finderPathFetchByG_U_CD_C_C_T_R, args);
		}
	}

	/**
	 * Creates a new social activity with the primary key. Does not add the social activity to the database.
	 *
	 * @param activityId the primary key for the new social activity
	 * @return the new social activity
	 */
	@Override
	public SocialActivity create(long activityId) {
		SocialActivity socialActivity = new SocialActivityImpl();

		socialActivity.setNew(true);
		socialActivity.setPrimaryKey(activityId);

		socialActivity.setCompanyId(CompanyThreadLocal.getCompanyId());

		return socialActivity;
	}

	/**
	 * Removes the social activity with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param activityId the primary key of the social activity
	 * @return the social activity that was removed
	 * @throws NoSuchActivityException if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity remove(long activityId)
		throws NoSuchActivityException {

		return remove((Serializable)activityId);
	}

	/**
	 * Removes the social activity with the primary key from the database. Also notifies the appropriate model listeners.
	 *
	 * @param primaryKey the primary key of the social activity
	 * @return the social activity that was removed
	 * @throws NoSuchActivityException if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity remove(Serializable primaryKey)
		throws NoSuchActivityException {

		Session session = null;

		try {
			session = openSession();

			SocialActivity socialActivity = (SocialActivity)session.get(
				SocialActivityImpl.class, primaryKey);

			if (socialActivity == null) {
				if (_log.isDebugEnabled()) {
					_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
				}

				throw new NoSuchActivityException(
					_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			return remove(socialActivity);
		}
		catch (NoSuchActivityException nsee) {
			throw nsee;
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}
	}

	@Override
	protected SocialActivity removeImpl(SocialActivity socialActivity) {
		Session session = null;

		try {
			session = openSession();

			if (!session.contains(socialActivity)) {
				socialActivity = (SocialActivity)session.get(
					SocialActivityImpl.class,
					socialActivity.getPrimaryKeyObj());
			}

			if (socialActivity != null) {
				session.delete(socialActivity);
			}
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}

		if (socialActivity != null) {
			clearCache(socialActivity);
		}

		return socialActivity;
	}

	@Override
	public SocialActivity updateImpl(SocialActivity socialActivity) {
		boolean isNew = socialActivity.isNew();

		if (!(socialActivity instanceof SocialActivityModelImpl)) {
			InvocationHandler invocationHandler = null;

			if (ProxyUtil.isProxyClass(socialActivity.getClass())) {
				invocationHandler = ProxyUtil.getInvocationHandler(
					socialActivity);

				throw new IllegalArgumentException(
					"Implement ModelWrapper in socialActivity proxy " +
						invocationHandler.getClass());
			}

			throw new IllegalArgumentException(
				"Implement ModelWrapper in custom SocialActivity implementation " +
					socialActivity.getClass());
		}

		SocialActivityModelImpl socialActivityModelImpl =
			(SocialActivityModelImpl)socialActivity;

		Session session = null;

		try {
			session = openSession();

			if (socialActivity.isNew()) {
				session.save(socialActivity);

				socialActivity.setNew(false);
			}
			else {
				socialActivity = (SocialActivity)session.merge(socialActivity);
			}
		}
		catch (Exception e) {
			throw processException(e);
		}
		finally {
			closeSession(session);
		}

		FinderCacheUtil.clearCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);

		if (!SocialActivityModelImpl.COLUMN_BITMASK_ENABLED) {
			FinderCacheUtil.clearCache(
				FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);
		}
		else if (isNew) {
			Object[] args = new Object[] {socialActivityModelImpl.getGroupId()};

			FinderCacheUtil.removeResult(_finderPathCountByGroupId, args);
			FinderCacheUtil.removeResult(
				_finderPathWithoutPaginationFindByGroupId, args);

			args = new Object[] {socialActivityModelImpl.getCompanyId()};

			FinderCacheUtil.removeResult(_finderPathCountByCompanyId, args);
			FinderCacheUtil.removeResult(
				_finderPathWithoutPaginationFindByCompanyId, args);

			args = new Object[] {socialActivityModelImpl.getUserId()};

			FinderCacheUtil.removeResult(_finderPathCountByUserId, args);
			FinderCacheUtil.removeResult(
				_finderPathWithoutPaginationFindByUserId, args);

			args = new Object[] {socialActivityModelImpl.getActivitySetId()};

			FinderCacheUtil.removeResult(_finderPathCountByActivitySetId, args);
			FinderCacheUtil.removeResult(
				_finderPathWithoutPaginationFindByActivitySetId, args);

			args = new Object[] {socialActivityModelImpl.getClassNameId()};

			FinderCacheUtil.removeResult(_finderPathCountByClassNameId, args);
			FinderCacheUtil.removeResult(
				_finderPathWithoutPaginationFindByClassNameId, args);

			args = new Object[] {socialActivityModelImpl.getReceiverUserId()};

			FinderCacheUtil.removeResult(
				_finderPathCountByReceiverUserId, args);
			FinderCacheUtil.removeResult(
				_finderPathWithoutPaginationFindByReceiverUserId, args);

			args = new Object[] {
				socialActivityModelImpl.getClassNameId(),
				socialActivityModelImpl.getClassPK()
			};

			FinderCacheUtil.removeResult(_finderPathCountByC_C, args);
			FinderCacheUtil.removeResult(
				_finderPathWithoutPaginationFindByC_C, args);

			args = new Object[] {
				socialActivityModelImpl.getMirrorActivityId(),
				socialActivityModelImpl.getClassNameId(),
				socialActivityModelImpl.getClassPK()
			};

			FinderCacheUtil.removeResult(_finderPathCountByM_C_C, args);
			FinderCacheUtil.removeResult(
				_finderPathWithoutPaginationFindByM_C_C, args);

			args = new Object[] {
				socialActivityModelImpl.getClassNameId(),
				socialActivityModelImpl.getClassPK(),
				socialActivityModelImpl.getType()
			};

			FinderCacheUtil.removeResult(_finderPathCountByC_C_T, args);
			FinderCacheUtil.removeResult(
				_finderPathWithoutPaginationFindByC_C_T, args);

			args = new Object[] {
				socialActivityModelImpl.getGroupId(),
				socialActivityModelImpl.getUserId(),
				socialActivityModelImpl.getClassNameId(),
				socialActivityModelImpl.getClassPK(),
				socialActivityModelImpl.getType(),
				socialActivityModelImpl.getReceiverUserId()
			};

			FinderCacheUtil.removeResult(_finderPathCountByG_U_C_C_T_R, args);
			FinderCacheUtil.removeResult(
				_finderPathWithoutPaginationFindByG_U_C_C_T_R, args);

			FinderCacheUtil.removeResult(
				_finderPathCountAll, FINDER_ARGS_EMPTY);
			FinderCacheUtil.removeResult(
				_finderPathWithoutPaginationFindAll, FINDER_ARGS_EMPTY);
		}
		else {
			if ((socialActivityModelImpl.getColumnBitmask() &
				 _finderPathWithoutPaginationFindByGroupId.
					 getColumnBitmask()) != 0) {

				Object[] args = new Object[] {
					socialActivityModelImpl.getOriginalGroupId()
				};

				FinderCacheUtil.removeResult(_finderPathCountByGroupId, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByGroupId, args);

				args = new Object[] {socialActivityModelImpl.getGroupId()};

				FinderCacheUtil.removeResult(_finderPathCountByGroupId, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByGroupId, args);
			}

			if ((socialActivityModelImpl.getColumnBitmask() &
				 _finderPathWithoutPaginationFindByCompanyId.
					 getColumnBitmask()) != 0) {

				Object[] args = new Object[] {
					socialActivityModelImpl.getOriginalCompanyId()
				};

				FinderCacheUtil.removeResult(_finderPathCountByCompanyId, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByCompanyId, args);

				args = new Object[] {socialActivityModelImpl.getCompanyId()};

				FinderCacheUtil.removeResult(_finderPathCountByCompanyId, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByCompanyId, args);
			}

			if ((socialActivityModelImpl.getColumnBitmask() &
				 _finderPathWithoutPaginationFindByUserId.getColumnBitmask()) !=
					 0) {

				Object[] args = new Object[] {
					socialActivityModelImpl.getOriginalUserId()
				};

				FinderCacheUtil.removeResult(_finderPathCountByUserId, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByUserId, args);

				args = new Object[] {socialActivityModelImpl.getUserId()};

				FinderCacheUtil.removeResult(_finderPathCountByUserId, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByUserId, args);
			}

			if ((socialActivityModelImpl.getColumnBitmask() &
				 _finderPathWithoutPaginationFindByActivitySetId.
					 getColumnBitmask()) != 0) {

				Object[] args = new Object[] {
					socialActivityModelImpl.getOriginalActivitySetId()
				};

				FinderCacheUtil.removeResult(
					_finderPathCountByActivitySetId, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByActivitySetId, args);

				args = new Object[] {
					socialActivityModelImpl.getActivitySetId()
				};

				FinderCacheUtil.removeResult(
					_finderPathCountByActivitySetId, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByActivitySetId, args);
			}

			if ((socialActivityModelImpl.getColumnBitmask() &
				 _finderPathWithoutPaginationFindByClassNameId.
					 getColumnBitmask()) != 0) {

				Object[] args = new Object[] {
					socialActivityModelImpl.getOriginalClassNameId()
				};

				FinderCacheUtil.removeResult(
					_finderPathCountByClassNameId, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByClassNameId, args);

				args = new Object[] {socialActivityModelImpl.getClassNameId()};

				FinderCacheUtil.removeResult(
					_finderPathCountByClassNameId, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByClassNameId, args);
			}

			if ((socialActivityModelImpl.getColumnBitmask() &
				 _finderPathWithoutPaginationFindByReceiverUserId.
					 getColumnBitmask()) != 0) {

				Object[] args = new Object[] {
					socialActivityModelImpl.getOriginalReceiverUserId()
				};

				FinderCacheUtil.removeResult(
					_finderPathCountByReceiverUserId, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByReceiverUserId, args);

				args = new Object[] {
					socialActivityModelImpl.getReceiverUserId()
				};

				FinderCacheUtil.removeResult(
					_finderPathCountByReceiverUserId, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByReceiverUserId, args);
			}

			if ((socialActivityModelImpl.getColumnBitmask() &
				 _finderPathWithoutPaginationFindByC_C.getColumnBitmask()) !=
					 0) {

				Object[] args = new Object[] {
					socialActivityModelImpl.getOriginalClassNameId(),
					socialActivityModelImpl.getOriginalClassPK()
				};

				FinderCacheUtil.removeResult(_finderPathCountByC_C, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByC_C, args);

				args = new Object[] {
					socialActivityModelImpl.getClassNameId(),
					socialActivityModelImpl.getClassPK()
				};

				FinderCacheUtil.removeResult(_finderPathCountByC_C, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByC_C, args);
			}

			if ((socialActivityModelImpl.getColumnBitmask() &
				 _finderPathWithoutPaginationFindByM_C_C.getColumnBitmask()) !=
					 0) {

				Object[] args = new Object[] {
					socialActivityModelImpl.getOriginalMirrorActivityId(),
					socialActivityModelImpl.getOriginalClassNameId(),
					socialActivityModelImpl.getOriginalClassPK()
				};

				FinderCacheUtil.removeResult(_finderPathCountByM_C_C, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByM_C_C, args);

				args = new Object[] {
					socialActivityModelImpl.getMirrorActivityId(),
					socialActivityModelImpl.getClassNameId(),
					socialActivityModelImpl.getClassPK()
				};

				FinderCacheUtil.removeResult(_finderPathCountByM_C_C, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByM_C_C, args);
			}

			if ((socialActivityModelImpl.getColumnBitmask() &
				 _finderPathWithoutPaginationFindByC_C_T.getColumnBitmask()) !=
					 0) {

				Object[] args = new Object[] {
					socialActivityModelImpl.getOriginalClassNameId(),
					socialActivityModelImpl.getOriginalClassPK(),
					socialActivityModelImpl.getOriginalType()
				};

				FinderCacheUtil.removeResult(_finderPathCountByC_C_T, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByC_C_T, args);

				args = new Object[] {
					socialActivityModelImpl.getClassNameId(),
					socialActivityModelImpl.getClassPK(),
					socialActivityModelImpl.getType()
				};

				FinderCacheUtil.removeResult(_finderPathCountByC_C_T, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByC_C_T, args);
			}

			if ((socialActivityModelImpl.getColumnBitmask() &
				 _finderPathWithoutPaginationFindByG_U_C_C_T_R.
					 getColumnBitmask()) != 0) {

				Object[] args = new Object[] {
					socialActivityModelImpl.getOriginalGroupId(),
					socialActivityModelImpl.getOriginalUserId(),
					socialActivityModelImpl.getOriginalClassNameId(),
					socialActivityModelImpl.getOriginalClassPK(),
					socialActivityModelImpl.getOriginalType(),
					socialActivityModelImpl.getOriginalReceiverUserId()
				};

				FinderCacheUtil.removeResult(
					_finderPathCountByG_U_C_C_T_R, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByG_U_C_C_T_R, args);

				args = new Object[] {
					socialActivityModelImpl.getGroupId(),
					socialActivityModelImpl.getUserId(),
					socialActivityModelImpl.getClassNameId(),
					socialActivityModelImpl.getClassPK(),
					socialActivityModelImpl.getType(),
					socialActivityModelImpl.getReceiverUserId()
				};

				FinderCacheUtil.removeResult(
					_finderPathCountByG_U_C_C_T_R, args);
				FinderCacheUtil.removeResult(
					_finderPathWithoutPaginationFindByG_U_C_C_T_R, args);
			}
		}

		EntityCacheUtil.putResult(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityImpl.class, socialActivity.getPrimaryKey(),
			socialActivity, false);

		clearUniqueFindersCache(socialActivityModelImpl, false);
		cacheUniqueFindersCache(socialActivityModelImpl);

		socialActivity.resetOriginalValues();

		return socialActivity;
	}

	/**
	 * Returns the social activity with the primary key or throws a <code>com.liferay.portal.kernel.exception.NoSuchModelException</code> if it could not be found.
	 *
	 * @param primaryKey the primary key of the social activity
	 * @return the social activity
	 * @throws NoSuchActivityException if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity findByPrimaryKey(Serializable primaryKey)
		throws NoSuchActivityException {

		SocialActivity socialActivity = fetchByPrimaryKey(primaryKey);

		if (socialActivity == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
			}

			throw new NoSuchActivityException(
				_NO_SUCH_ENTITY_WITH_PRIMARY_KEY + primaryKey);
		}

		return socialActivity;
	}

	/**
	 * Returns the social activity with the primary key or throws a <code>NoSuchActivityException</code> if it could not be found.
	 *
	 * @param activityId the primary key of the social activity
	 * @return the social activity
	 * @throws NoSuchActivityException if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity findByPrimaryKey(long activityId)
		throws NoSuchActivityException {

		return findByPrimaryKey((Serializable)activityId);
	}

	/**
	 * Returns the social activity with the primary key or returns <code>null</code> if it could not be found.
	 *
	 * @param activityId the primary key of the social activity
	 * @return the social activity, or <code>null</code> if a social activity with the primary key could not be found
	 */
	@Override
	public SocialActivity fetchByPrimaryKey(long activityId) {
		return fetchByPrimaryKey((Serializable)activityId);
	}

	/**
	 * Returns all the social activities.
	 *
	 * @return the social activities
	 */
	@Override
	public List<SocialActivity> findAll() {
		return findAll(QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);
	}

	/**
	 * Returns a range of all the social activities.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @return the range of social activities
	 */
	@Override
	public List<SocialActivity> findAll(int start, int end) {
		return findAll(start, end, null);
	}

	/**
	 * Returns an ordered range of all the social activities.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @return the ordered range of social activities
	 */
	@Override
	public List<SocialActivity> findAll(
		int start, int end,
		OrderByComparator<SocialActivity> orderByComparator) {

		return findAll(start, end, orderByComparator, true);
	}

	/**
	 * Returns an ordered range of all the social activities.
	 *
	 * <p>
	 * Useful when paginating results. Returns a maximum of <code>end - start</code> instances. <code>start</code> and <code>end</code> are not primary keys, they are indexes in the result set. Thus, <code>0</code> refers to the first result in the set. Setting both <code>start</code> and <code>end</code> to <code>QueryUtil#ALL_POS</code> will return the full result set. If <code>orderByComparator</code> is specified, then the query will include the given ORDER BY logic. If <code>orderByComparator</code> is absent and pagination is required (<code>start</code> and <code>end</code> are not <code>QueryUtil#ALL_POS</code>), then the query will include the default ORDER BY logic from <code>SocialActivityModelImpl</code>. If both <code>orderByComparator</code> and pagination are absent, for performance reasons, the query will not have an ORDER BY clause and the returned result set will be sorted on by the primary key in an ascending order.
	 * </p>
	 *
	 * @param start the lower bound of the range of social activities
	 * @param end the upper bound of the range of social activities (not inclusive)
	 * @param orderByComparator the comparator to order the results by (optionally <code>null</code>)
	 * @param useFinderCache whether to use the finder cache
	 * @return the ordered range of social activities
	 */
	@Override
	public List<SocialActivity> findAll(
		int start, int end, OrderByComparator<SocialActivity> orderByComparator,
		boolean useFinderCache) {

		boolean pagination = true;
		FinderPath finderPath = null;
		Object[] finderArgs = null;

		if ((start == QueryUtil.ALL_POS) && (end == QueryUtil.ALL_POS) &&
			(orderByComparator == null)) {

			pagination = false;

			if (useFinderCache) {
				finderPath = _finderPathWithoutPaginationFindAll;
				finderArgs = FINDER_ARGS_EMPTY;
			}
		}
		else if (useFinderCache) {
			finderPath = _finderPathWithPaginationFindAll;
			finderArgs = new Object[] {start, end, orderByComparator};
		}

		List<SocialActivity> list = null;

		if (useFinderCache) {
			list = (List<SocialActivity>)FinderCacheUtil.getResult(
				finderPath, finderArgs, this);
		}

		if (list == null) {
			StringBundler query = null;
			String sql = null;

			if (orderByComparator != null) {
				query = new StringBundler(
					2 + (orderByComparator.getOrderByFields().length * 2));

				query.append(_SQL_SELECT_SOCIALACTIVITY);

				appendOrderByComparator(
					query, _ORDER_BY_ENTITY_ALIAS, orderByComparator);

				sql = query.toString();
			}
			else {
				sql = _SQL_SELECT_SOCIALACTIVITY;

				if (pagination) {
					sql = sql.concat(SocialActivityModelImpl.ORDER_BY_JPQL);
				}
			}

			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(sql);

				if (!pagination) {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end, false);

					Collections.sort(list);

					list = Collections.unmodifiableList(list);
				}
				else {
					list = (List<SocialActivity>)QueryUtil.list(
						q, getDialect(), start, end);
				}

				cacheResult(list);

				if (useFinderCache) {
					FinderCacheUtil.putResult(finderPath, finderArgs, list);
				}
			}
			catch (Exception e) {
				if (useFinderCache) {
					FinderCacheUtil.removeResult(finderPath, finderArgs);
				}

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return list;
	}

	/**
	 * Removes all the social activities from the database.
	 *
	 */
	@Override
	public void removeAll() {
		for (SocialActivity socialActivity : findAll()) {
			remove(socialActivity);
		}
	}

	/**
	 * Returns the number of social activities.
	 *
	 * @return the number of social activities
	 */
	@Override
	public int countAll() {
		Long count = (Long)FinderCacheUtil.getResult(
			_finderPathCountAll, FINDER_ARGS_EMPTY, this);

		if (count == null) {
			Session session = null;

			try {
				session = openSession();

				Query q = session.createQuery(_SQL_COUNT_SOCIALACTIVITY);

				count = (Long)q.uniqueResult();

				FinderCacheUtil.putResult(
					_finderPathCountAll, FINDER_ARGS_EMPTY, count);
			}
			catch (Exception e) {
				FinderCacheUtil.removeResult(
					_finderPathCountAll, FINDER_ARGS_EMPTY);

				throw processException(e);
			}
			finally {
				closeSession(session);
			}
		}

		return count.intValue();
	}

	@Override
	public Set<String> getBadColumnNames() {
		return _badColumnNames;
	}

	@Override
	protected EntityCache getEntityCache() {
		return EntityCacheUtil.getEntityCache();
	}

	@Override
	protected String getPKDBName() {
		return "activityId";
	}

	@Override
	protected String getSelectSQL() {
		return _SQL_SELECT_SOCIALACTIVITY;
	}

	@Override
	protected Map<String, Integer> getTableColumnsMap() {
		return SocialActivityModelImpl.TABLE_COLUMNS_MAP;
	}

	/**
	 * Initializes the social activity persistence.
	 */
	public void afterPropertiesSet() {
		_finderPathWithPaginationFindAll = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
			"findAll", new String[0]);

		_finderPathWithoutPaginationFindAll = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"findAll", new String[0]);

		_finderPathCountAll = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countAll",
			new String[0]);

		_finderPathWithPaginationFindByGroupId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
			"findByGroupId",
			new String[] {
				Long.class.getName(), Integer.class.getName(),
				Integer.class.getName(), OrderByComparator.class.getName()
			});

		_finderPathWithoutPaginationFindByGroupId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"findByGroupId", new String[] {Long.class.getName()},
			SocialActivityModelImpl.GROUPID_COLUMN_BITMASK |
			SocialActivityModelImpl.CREATEDATE_COLUMN_BITMASK);

		_finderPathCountByGroupId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByGroupId",
			new String[] {Long.class.getName()});

		_finderPathWithPaginationFindByCompanyId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
			"findByCompanyId",
			new String[] {
				Long.class.getName(), Integer.class.getName(),
				Integer.class.getName(), OrderByComparator.class.getName()
			});

		_finderPathWithoutPaginationFindByCompanyId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"findByCompanyId", new String[] {Long.class.getName()},
			SocialActivityModelImpl.COMPANYID_COLUMN_BITMASK |
			SocialActivityModelImpl.CREATEDATE_COLUMN_BITMASK);

		_finderPathCountByCompanyId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByCompanyId",
			new String[] {Long.class.getName()});

		_finderPathWithPaginationFindByUserId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
			"findByUserId",
			new String[] {
				Long.class.getName(), Integer.class.getName(),
				Integer.class.getName(), OrderByComparator.class.getName()
			});

		_finderPathWithoutPaginationFindByUserId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"findByUserId", new String[] {Long.class.getName()},
			SocialActivityModelImpl.USERID_COLUMN_BITMASK |
			SocialActivityModelImpl.CREATEDATE_COLUMN_BITMASK);

		_finderPathCountByUserId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByUserId",
			new String[] {Long.class.getName()});

		_finderPathWithPaginationFindByActivitySetId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
			"findByActivitySetId",
			new String[] {
				Long.class.getName(), Integer.class.getName(),
				Integer.class.getName(), OrderByComparator.class.getName()
			});

		_finderPathWithoutPaginationFindByActivitySetId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"findByActivitySetId", new String[] {Long.class.getName()},
			SocialActivityModelImpl.ACTIVITYSETID_COLUMN_BITMASK |
			SocialActivityModelImpl.CREATEDATE_COLUMN_BITMASK);

		_finderPathCountByActivitySetId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByActivitySetId",
			new String[] {Long.class.getName()});

		_finderPathFetchByMirrorActivityId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_ENTITY,
			"fetchByMirrorActivityId", new String[] {Long.class.getName()},
			SocialActivityModelImpl.MIRRORACTIVITYID_COLUMN_BITMASK);

		_finderPathCountByMirrorActivityId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"countByMirrorActivityId", new String[] {Long.class.getName()});

		_finderPathWithPaginationFindByClassNameId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
			"findByClassNameId",
			new String[] {
				Long.class.getName(), Integer.class.getName(),
				Integer.class.getName(), OrderByComparator.class.getName()
			});

		_finderPathWithoutPaginationFindByClassNameId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"findByClassNameId", new String[] {Long.class.getName()},
			SocialActivityModelImpl.CLASSNAMEID_COLUMN_BITMASK |
			SocialActivityModelImpl.CREATEDATE_COLUMN_BITMASK);

		_finderPathCountByClassNameId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByClassNameId",
			new String[] {Long.class.getName()});

		_finderPathWithPaginationFindByReceiverUserId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
			"findByReceiverUserId",
			new String[] {
				Long.class.getName(), Integer.class.getName(),
				Integer.class.getName(), OrderByComparator.class.getName()
			});

		_finderPathWithoutPaginationFindByReceiverUserId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"findByReceiverUserId", new String[] {Long.class.getName()},
			SocialActivityModelImpl.RECEIVERUSERID_COLUMN_BITMASK |
			SocialActivityModelImpl.CREATEDATE_COLUMN_BITMASK);

		_finderPathCountByReceiverUserId = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByReceiverUserId",
			new String[] {Long.class.getName()});

		_finderPathWithPaginationFindByC_C = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
			"findByC_C",
			new String[] {
				Long.class.getName(), Long.class.getName(),
				Integer.class.getName(), Integer.class.getName(),
				OrderByComparator.class.getName()
			});

		_finderPathWithoutPaginationFindByC_C = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"findByC_C",
			new String[] {Long.class.getName(), Long.class.getName()},
			SocialActivityModelImpl.CLASSNAMEID_COLUMN_BITMASK |
			SocialActivityModelImpl.CLASSPK_COLUMN_BITMASK |
			SocialActivityModelImpl.CREATEDATE_COLUMN_BITMASK);

		_finderPathCountByC_C = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByC_C",
			new String[] {Long.class.getName(), Long.class.getName()});

		_finderPathWithPaginationFindByM_C_C = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
			"findByM_C_C",
			new String[] {
				Long.class.getName(), Long.class.getName(),
				Long.class.getName(), Integer.class.getName(),
				Integer.class.getName(), OrderByComparator.class.getName()
			});

		_finderPathWithoutPaginationFindByM_C_C = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"findByM_C_C",
			new String[] {
				Long.class.getName(), Long.class.getName(), Long.class.getName()
			},
			SocialActivityModelImpl.MIRRORACTIVITYID_COLUMN_BITMASK |
			SocialActivityModelImpl.CLASSNAMEID_COLUMN_BITMASK |
			SocialActivityModelImpl.CLASSPK_COLUMN_BITMASK |
			SocialActivityModelImpl.CREATEDATE_COLUMN_BITMASK);

		_finderPathCountByM_C_C = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByM_C_C",
			new String[] {
				Long.class.getName(), Long.class.getName(), Long.class.getName()
			});

		_finderPathWithPaginationFindByC_C_T = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
			"findByC_C_T",
			new String[] {
				Long.class.getName(), Long.class.getName(),
				Integer.class.getName(), Integer.class.getName(),
				Integer.class.getName(), OrderByComparator.class.getName()
			});

		_finderPathWithoutPaginationFindByC_C_T = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"findByC_C_T",
			new String[] {
				Long.class.getName(), Long.class.getName(),
				Integer.class.getName()
			},
			SocialActivityModelImpl.CLASSNAMEID_COLUMN_BITMASK |
			SocialActivityModelImpl.CLASSPK_COLUMN_BITMASK |
			SocialActivityModelImpl.TYPE_COLUMN_BITMASK |
			SocialActivityModelImpl.CREATEDATE_COLUMN_BITMASK);

		_finderPathCountByC_C_T = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByC_C_T",
			new String[] {
				Long.class.getName(), Long.class.getName(),
				Integer.class.getName()
			});

		_finderPathWithPaginationFindByG_U_C_C_T_R = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITH_PAGINATION,
			"findByG_U_C_C_T_R",
			new String[] {
				Long.class.getName(), Long.class.getName(),
				Long.class.getName(), Long.class.getName(),
				Integer.class.getName(), Long.class.getName(),
				Integer.class.getName(), Integer.class.getName(),
				OrderByComparator.class.getName()
			});

		_finderPathWithoutPaginationFindByG_U_C_C_T_R = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION,
			"findByG_U_C_C_T_R",
			new String[] {
				Long.class.getName(), Long.class.getName(),
				Long.class.getName(), Long.class.getName(),
				Integer.class.getName(), Long.class.getName()
			},
			SocialActivityModelImpl.GROUPID_COLUMN_BITMASK |
			SocialActivityModelImpl.USERID_COLUMN_BITMASK |
			SocialActivityModelImpl.CLASSNAMEID_COLUMN_BITMASK |
			SocialActivityModelImpl.CLASSPK_COLUMN_BITMASK |
			SocialActivityModelImpl.TYPE_COLUMN_BITMASK |
			SocialActivityModelImpl.RECEIVERUSERID_COLUMN_BITMASK |
			SocialActivityModelImpl.CREATEDATE_COLUMN_BITMASK);

		_finderPathCountByG_U_C_C_T_R = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByG_U_C_C_T_R",
			new String[] {
				Long.class.getName(), Long.class.getName(),
				Long.class.getName(), Long.class.getName(),
				Integer.class.getName(), Long.class.getName()
			});

		_finderPathFetchByG_U_CD_C_C_T_R = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED,
			SocialActivityImpl.class, FINDER_CLASS_NAME_ENTITY,
			"fetchByG_U_CD_C_C_T_R",
			new String[] {
				Long.class.getName(), Long.class.getName(),
				Long.class.getName(), Long.class.getName(),
				Long.class.getName(), Integer.class.getName(),
				Long.class.getName()
			},
			SocialActivityModelImpl.GROUPID_COLUMN_BITMASK |
			SocialActivityModelImpl.USERID_COLUMN_BITMASK |
			SocialActivityModelImpl.CREATEDATE_COLUMN_BITMASK |
			SocialActivityModelImpl.CLASSNAMEID_COLUMN_BITMASK |
			SocialActivityModelImpl.CLASSPK_COLUMN_BITMASK |
			SocialActivityModelImpl.TYPE_COLUMN_BITMASK |
			SocialActivityModelImpl.RECEIVERUSERID_COLUMN_BITMASK);

		_finderPathCountByG_U_CD_C_C_T_R = new FinderPath(
			SocialActivityModelImpl.ENTITY_CACHE_ENABLED,
			SocialActivityModelImpl.FINDER_CACHE_ENABLED, Long.class,
			FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION, "countByG_U_CD_C_C_T_R",
			new String[] {
				Long.class.getName(), Long.class.getName(),
				Long.class.getName(), Long.class.getName(),
				Long.class.getName(), Integer.class.getName(),
				Long.class.getName()
			});
	}

	public void destroy() {
		EntityCacheUtil.removeCache(SocialActivityImpl.class.getName());
		FinderCacheUtil.removeCache(FINDER_CLASS_NAME_ENTITY);
		FinderCacheUtil.removeCache(FINDER_CLASS_NAME_LIST_WITH_PAGINATION);
		FinderCacheUtil.removeCache(FINDER_CLASS_NAME_LIST_WITHOUT_PAGINATION);
	}

	private static final String _SQL_SELECT_SOCIALACTIVITY =
		"SELECT socialActivity FROM SocialActivity socialActivity";

	private static final String _SQL_SELECT_SOCIALACTIVITY_WHERE =
		"SELECT socialActivity FROM SocialActivity socialActivity WHERE ";

	private static final String _SQL_COUNT_SOCIALACTIVITY =
		"SELECT COUNT(socialActivity) FROM SocialActivity socialActivity";

	private static final String _SQL_COUNT_SOCIALACTIVITY_WHERE =
		"SELECT COUNT(socialActivity) FROM SocialActivity socialActivity WHERE ";

	private static final String _ORDER_BY_ENTITY_ALIAS = "socialActivity.";

	private static final String _NO_SUCH_ENTITY_WITH_PRIMARY_KEY =
		"No SocialActivity exists with the primary key ";

	private static final String _NO_SUCH_ENTITY_WITH_KEY =
		"No SocialActivity exists with the key {";

	private static final Log _log = LogFactoryUtil.getLog(
		SocialActivityPersistenceImpl.class);

	private static final Set<String> _badColumnNames = SetUtil.fromArray(
		new String[] {"type"});

}