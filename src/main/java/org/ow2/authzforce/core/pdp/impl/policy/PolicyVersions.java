/**
 * Copyright (C) 2012-2016 Thales Services SAS.
 *
 * This file is part of AuthZForce CE.
 *
 * AuthZForce CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuthZForce CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AuthZForce CE.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.authzforce.core.pdp.impl.policy;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;

import org.ow2.authzforce.core.pdp.api.policy.PolicyVersion;
import org.ow2.authzforce.core.pdp.api.policy.VersionPatterns;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.UnmodifiableIterator;

/**
 * Policy versions sorted from latest version to oldest.
 * <p>
 * The choice to have the latest version in first position is motivated by §5.10 of XACML core spec:
 * "In the case that more than one matching version can be obtained, then the most recent one SHOULD be used."
 *
 * @param <P>
 *            policy type (or any other type of data corresponding to a specific policy version)
 * 
 * @version $Id: $
 */
public final class PolicyVersions<P> implements Iterable<Entry<PolicyVersion, P>>
{
	private final ImmutableSortedMap<PolicyVersion, P> policiesByVersion;

	/**
	 * Creates instance
	 *
	 * @param versions
	 *            policy versions
	 */
	public PolicyVersions(final Map<PolicyVersion, P> versions)
	{
		policiesByVersion = versions == null ? ImmutableSortedMap.<PolicyVersion, P> of() : ImmutableSortedMap.copyOf(versions, Collections.reverseOrder());
	}

	/**
	 * Get policy in a specific version
	 *
	 * @param version
	 *            policy version
	 * @return policy
	 */
	public P get(final PolicyVersion version)
	{
		return policiesByVersion.get(version);
	}

	/**
	 * Get latest policy version matching specific version patterns
	 *
	 * @param versionPatterns
	 *            version patterns
	 * @return latest version; null if none matched
	 */
	public Entry<PolicyVersion, P> getLatest(final VersionPatterns versionPatterns)
	{
		// policiesByVersion is not empty -> at least one value
		final Iterator<Entry<PolicyVersion, P>> versionPolicyPairsIterator = policiesByVersion.entrySet().iterator();
		if (versionPatterns == null)
		{
			/*
			 * Return the latest version which is the first element by design (TreeMap initialized with reverse order on version keys). See §5.10 of XACML core spec:
			 * "In the case that more than one matching version can be obtained, then the most recent one SHOULD be used."
			 */
			return versionPolicyPairsIterator.next();
		}

		// constraints not null
		// in the loop, go on until LatestVersion matched, then go on as long as
		// EarliestVersion
		// matched, if Version matched, return the result
		boolean latestVersionMatched = false;
		boolean earliestVersionMatched = false;
		while (versionPolicyPairsIterator.hasNext())
		{
			final Entry<PolicyVersion, P> versionPolicyPair = versionPolicyPairsIterator.next();
			final PolicyVersion version = versionPolicyPair.getKey();
			/*
			 * Versions ordered by latest first, so check against constraints' LatestVersion pattern first. If LatestVersion is matched by this version, no need to check again for the next versions,
			 * as they are already sorted from latest to earliest. If LatestVersion not matched yet, we check now.
			 */
			if (!latestVersionMatched)
			{
				latestVersionMatched = versionPatterns.matchLatestVersion(version);
			}

			// If LatestVersion matched, check other constraints, else do
			// nothing (check next
			// version)
			if (latestVersionMatched)
			{
				/*
				 * If EarliestVersion already checked and not matched before, we would have returned null (see below). So at this point, EarliestVersion is either not checked yet or already matched.
				 * So EarliestVersion no checked iff not already matched.
				 */
				if (!earliestVersionMatched)
				{
					// EarliestVersion not checked yet
					// check against EarliestVersion pattern
					earliestVersionMatched = versionPatterns.matchEarliestVersion(version);
					/*
					 * If still not matched, version cannot be in the [EarliestVersion, LatestVersion] interval. All next versions are earlier, so they cannot be either -> no match
					 */
					if (!earliestVersionMatched)
					{
						return null;
					}
				}

				// EarliestVersion and LatestVersion matched.
				// Check against Version pattern
				if (versionPatterns.matchVersion(version))
				{
					// all constraints matched, return the associated policy
					return versionPolicyPair;
				}

				// constraints not met, so try next version
			}
		}

		// no match found
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<Entry<PolicyVersion, P>> iterator()
	{
		return policiesByVersion.entrySet().iterator();
	}

	/**
	 * Get number of versions
	 * 
	 * @return number of policy versions
	 */
	public int size()
	{
		return this.policiesByVersion.size();
	}

	/**
	 * Get iterator over versions from oldest to latest
	 * 
	 * @return unmodifiable iterator over versions from oldest to latest
	 */
	public UnmodifiableIterator<Entry<PolicyVersion, P>> oldestToLatestIterator()
	{
		/*
		 * The map is sorted from latest to oldest by default, so "descending" in this case means from oldest to latest
		 */
		return policiesByVersion.descendingMap().entrySet().iterator();
	}

	/**
	 * Get versions from latest to oldest
	 * 
	 * @return versions from latest to oldest
	 */
	public NavigableSet<PolicyVersion> latestToOldestSet()
	{
		return policiesByVersion.keySet();
	}
}
