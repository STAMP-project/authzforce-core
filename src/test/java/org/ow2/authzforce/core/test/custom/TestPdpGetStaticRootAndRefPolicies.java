/**
 * Copyright (C) 2012-2015 Thales Services SAS.
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
package org.ow2.authzforce.core.test.custom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBException;

import oasis.names.tc.xacml._3_0.core.schema.wd_17.IdReferenceType;

import org.junit.Test;
import org.ow2.authzforce.core.pdp.api.PolicyVersion;
import org.ow2.authzforce.core.pdp.impl.PDPImpl;
import org.ow2.authzforce.core.test.utils.PdpTest;
import org.ow2.authzforce.core.test.utils.TestUtils;

/**
 * Test of {@link PDPImpl#getStaticRootAndRefPolicies()}
 *
 */
public class TestPdpGetStaticRootAndRefPolicies
{
	/**
	 * Name of directory that contains test resources for each test
	 */
	public final static String TEST_RESOURCES_DIRECTORY_LOCATION = "classpath:conformance/others/PolicyReference.Valid";
	private final static Set<IdReferenceType> POLICY_ID_REFS;
	static
	{
		POLICY_ID_REFS = new HashSet<>();
		POLICY_ID_REFS.add(new IdReferenceType("root:policyset-with-refs", "1.0", null, null));
		POLICY_ID_REFS.add(new IdReferenceType("PPS:Employee", "1.0", null, null));
	}

	@Test
	public void test() throws IllegalArgumentException, IOException, URISyntaxException, JAXBException
	{
		final String testResourceLocationPrefix = TEST_RESOURCES_DIRECTORY_LOCATION + "/";
		// Create PDP
		try (PDPImpl pdp = TestUtils.getPDPNewInstance(testResourceLocationPrefix + PdpTest.POLICY_FILENAME,
				testResourceLocationPrefix + PdpTest.REF_POLICIES_DIR_NAME, false, null, null))
		{
			final Map<String, PolicyVersion> staticRootAndRefPolicyMap = pdp.getStaticRootAndRefPolicies();
			assertEquals("Invalid number of policies returned by PDPImpl#getStaticRootAndRefPolicies()",
					POLICY_ID_REFS.size(), staticRootAndRefPolicyMap.size());
			for (final Entry<String, PolicyVersion> policyEntry : pdp.getStaticRootAndRefPolicies().entrySet())
			{
				assertTrue("Unexpected policy return by PDPImpl#getStaticRootAndRefPolicies()",
						POLICY_ID_REFS.contains(new IdReferenceType(policyEntry.getKey(), policyEntry.getValue()
								.toString(), null, null)));
			}

		}
	}

}