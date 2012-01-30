/**
 *
 *
 * Copyright (c) 2010 / 2011 eZuce, Inc. All rights reserved.
 * Contributed to SIPfoundry under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.openacd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easymock.EasyMock;
import org.sipfoundry.commons.mongo.MongoConstants;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.ExtensionInUseException;
import org.sipfoundry.sipxconfig.common.NameInUseException;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.common.UserException;
import org.sipfoundry.sipxconfig.commserver.LocationsManager;
import org.sipfoundry.sipxconfig.commserver.imdb.MongoTestCaseHelper;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchAction;
import org.sipfoundry.sipxconfig.freeswitch.FreeswitchCondition;
import org.sipfoundry.sipxconfig.freeswitch.config.DefaultContextConfigurationTest;
import org.sipfoundry.sipxconfig.openacd.OpenAcdContextImpl.ClientInUseException;
import org.sipfoundry.sipxconfig.openacd.OpenAcdContextImpl.DefaultAgentGroupDeleteException;
import org.sipfoundry.sipxconfig.openacd.OpenAcdContextImpl.QueueGroupInUseException;
import org.sipfoundry.sipxconfig.openacd.OpenAcdContextImpl.QueueInUseException;
import org.sipfoundry.sipxconfig.openacd.OpenAcdContextImpl.SkillInUseException;
import org.sipfoundry.sipxconfig.openacd.OpenAcdRecipeAction.ACTION;
import org.sipfoundry.sipxconfig.openacd.OpenAcdRecipeCondition.CONDITION;
import org.sipfoundry.sipxconfig.openacd.OpenAcdRecipeStep.FREQUENCY;
import org.sipfoundry.sipxconfig.test.IntegrationTestCase;
import org.sipfoundry.sipxconfig.test.TestHelper;
import org.springframework.dao.support.DataAccessUtils;

import com.mongodb.Mongo;

/**
 * Disabled - There were many errors. I need to discuss changes w/OpenACD group. -- Douglas
 */
public class OpenAcdContextTestDisabled extends IntegrationTestCase {
/*    private OpenAcdContext m_openAcdContext;
    private static String[][] ACTIONS = {
        {
            "answer", ""
        }, {
            "set", "domain_name=$${domain}"
        }, {
            "set", "brand=1"
        }, {
            "set", "queue=Sales"
        }, {
            "set", "allow_voicemail=true"
        }, {
            "erlang_sendmsg", "freeswitch_media_manager testme@192.168.1.1 inivr ${uuid}"
        }, {
            "playback", "/usr/share/www/doc/stdprompts/welcome.wav"
        }, {
            "erlang", "freeswitch_media_manager:! testme@192.168.1.1"
        },
    };
    private OpenAcdContextImpl m_openAcdContextImpl;

    private CoreContext m_coreContext;
    private LocationsManager m_locationsManager;
    private OpenAcdSkillGroupMigrationContext m_migrationContext;

    @Override
    protected void onSetUpInTransaction() throws Exception {
        loadDataSetXml("admin/commserver/seedLocations.xml");
        loadDataSetXml("domain/DomainSeed.xml");
        m_migrationContext.migrateSkillGroup();
        getEntityCollection().drop();
    }
    
    public static OpenAcdLine createOpenAcdLine(String extensionName) {
        OpenAcdLine extension = new OpenAcdLine();
        extension.setName(extensionName);

        FreeswitchCondition condition = new FreeswitchCondition();
        condition.setField("destination_number");
        condition.setExpression("^300$");
        extension.addCondition(condition);

        for (int i = 0; i < ACTIONS.length; i++) {
            FreeswitchAction action = new FreeswitchAction();
            action.setApplication(ACTIONS[i][0]);
            action.setData(ACTIONS[i][1]);
            condition.addAction(action);
        }
        return extension;
    }    

    // test save open acd extension
    public void testOpenAcdLineCrud() throws Exception {
        assertEquals(0, m_openAcdContextImpl.getLines().size());
        OpenAcdLine extension = createOpenAcdLine("example");
        extension.setAlias("alias");
        extension.setDid("1234567890");

        m_openAcdContext.saveExtension(extension);
        assertEquals(1, m_openAcdContext.getLines().size());
        m_openAcdContext.saveExtension(extension);
        // test save extension with same name
        try {
            OpenAcdLine sameNameExtension = new OpenAcdLine();
            sameNameExtension.setName("example");
            FreeswitchCondition condition = new FreeswitchCondition();
            condition.setField("destination_number");
            condition.setExpression("^301$");
            sameNameExtension.addCondition(condition);
            m_openAcdContext.saveExtension(sameNameExtension);
            fail();
        } catch (NameInUseException ex) {
        }

        OpenAcdLine line2 = new OpenAcdLine();
        line2.setName("example1");
        FreeswitchCondition condition2 = new FreeswitchCondition();
        condition2.setField("destination_number");
        condition2.setExpression("^301$");
        line2.addCondition(condition2);
        try {
            line2.setAlias("alias");// existing alias
            m_openAcdContext.saveExtension(line2);
            fail();
        } catch (ExtensionInUseException ex) {
        }
        try {
            line2.setAlias("300");// existing alias
            m_openAcdContext.saveExtension(line2);
            fail();
        } catch (ExtensionInUseException ex) {
        }
        try {
            line2.setAlias("");
            line2.setDid("alias");
            m_openAcdContext.saveExtension(line2);
            fail();
        } catch (ExtensionInUseException ex) {
        }
        try {
            line2.setDid("300");
            m_openAcdContext.saveExtension(line2);
            fail();
        } catch (ExtensionInUseException ex) {
        }
        try {
            line2.setAlias("alias");// existing alias
            m_openAcdContext.saveExtension(line2);
            fail();
        } catch (ExtensionInUseException ex) {
        }

        // test get extension by name
        OpenAcdLine savedExtension = (OpenAcdLine) m_openAcdContext.getExtensionByName("example");
        assertNotNull(extension);
        assertEquals("example", savedExtension.getName());
        // test modify extension without changing name
        try {
            m_openAcdContext.saveExtension(savedExtension);
        } catch (NameInUseException ex) {
            fail();
        }

        // test get extension by id
        Integer id = savedExtension.getId();
        OpenAcdLine extensionById = (OpenAcdLine) m_openAcdContext.getExtensionById(id);
        assertNotNull(extensionById);
        assertEquals("example", extensionById.getName());

        // test saved conditions and actions
        Set<FreeswitchCondition> conditions = extensionById.getConditions();
        assertEquals(1, conditions.size());
        for (FreeswitchCondition condition : conditions) {
            assertEquals(8, condition.getActions().size());
            List<FreeswitchAction> actions = new LinkedList<FreeswitchAction>();
            actions.addAll(condition.getActions());
            assertEquals("answer", actions.get(0).getApplication());
            assertEquals("", actions.get(0).getData());
            assertEquals("set", actions.get(1).getApplication());
            assertEquals("domain_name=$${domain}", actions.get(1).getData());
            assertEquals("set", actions.get(2).getApplication());
            assertEquals("brand=1", actions.get(2).getData());
            assertEquals("set", actions.get(3).getApplication());
            assertEquals("queue=Sales", actions.get(3).getData());
            assertEquals("set", actions.get(4).getApplication());
            assertEquals("allow_voicemail=true", actions.get(4).getData());
            assertEquals("erlang_sendmsg", actions.get(5).getApplication());
            assertEquals("freeswitch_media_manager testme@192.168.1.1 inivr ${uuid}", actions.get(5).getData());
            assertEquals("playback", actions.get(6).getApplication());
            assertEquals("/usr/share/www/doc/stdprompts/welcome.wav", actions.get(6).getData());
            assertEquals("erlang", actions.get(7).getApplication());
            assertEquals("freeswitch_media_manager:! testme@192.168.1.1", actions.get(7).getData());
        }

        // test remove extension
        assertEquals(1, m_openAcdContext.getLines().size());
        m_openAcdContext.deleteExtension(extensionById);
        assertEquals(0, m_openAcdContext.getLines().size());
    }

    public void testOpenAcdCommandCrud() throws Exception {
        // test existing 'login', 'logout', 'go available' and 'release' default commands
        assertEquals(4, m_openAcdContext.getCommands().size());


        loadDataSetXml("commserver/seedLocations.xml");
        loadDataSetXml("domain/DomainSeed.xml");

        // test save open acd extension
        OpenAcdCommand command = new OpenAcdCommand();
        command.setName("test");
        FreeswitchCondition fscondition = new FreeswitchCondition();
        fscondition.setField("destination_number");
        fscondition.setExpression("^*98$");
        //fscondition.getActions().addAll((OpenAcdCommand.getDefaultActions(location)));
        command.addCondition(fscondition);
        m_openAcdContext.saveExtension(command);
        assertEquals(5, m_openAcdContext.getCommands().size());

        // test save extension with same name
        try {
            OpenAcdCommand sameNameExtension = new OpenAcdCommand();
            sameNameExtension.setName("test");
            FreeswitchCondition condition1 = new FreeswitchCondition();
            condition1.setField("destination_number");
            condition1.setExpression("^*99$");
            sameNameExtension.addCondition(condition1);
            m_openAcdContext.saveExtension(sameNameExtension);
            fail();
        } catch (NameInUseException ex) {
        }

        try {
            OpenAcdExtension extension = new OpenAcdExtension();
            extension.setName("tralala");
            FreeswitchCondition condition1 = new FreeswitchCondition();
            condition1.setField("destination_number");
            condition1.setExpression("^*98$");
            extension.addCondition(condition1);
            m_openAcdContext.saveExtension(extension);
            fail();
        } catch (ExtensionInUseException ex) {
        }

        // test get extension by name
        OpenAcdCommand savedExtension = (OpenAcdCommand) m_openAcdContext.getExtensionByName("test");
        assertNotNull(command);
        assertEquals("test", savedExtension.getName());
        // test modify extension without changing name
        try {
            m_openAcdContext.saveExtension(savedExtension);
        } catch (NameInUseException ex) {
            fail();
        }

        // test get extension by id
        Integer id = savedExtension.getId();
        OpenAcdCommand extensionById = (OpenAcdCommand) m_openAcdContext.getExtensionById(id);
        assertNotNull(extensionById);
        assertEquals("test", extensionById.getName());

        // test saved conditions and actions
        Set<FreeswitchCondition> conditions = extensionById.getConditions();
        assertEquals(1, conditions.size());
        for (FreeswitchCondition condition : conditions) {
            assertEquals(4, condition.getActions().size());
            List<FreeswitchAction> actions = new LinkedList<FreeswitchAction>();
            actions.addAll(condition.getActions());
            assertEquals("erlang_sendmsg", actions.get(0).getApplication());
            assertEquals("agent_dialplan_listener  openacd@" + location.getFqdn()
                    + " agent_login ${sip_from_user} pstn ${sip_from_uri}", actions.get(0).getData());
            assertEquals("answer", actions.get(1).getApplication());
            assertNull(actions.get(1).getData());
            assertEquals("sleep", actions.get(2).getApplication());
            assertEquals("2000", actions.get(2).getData());
            assertEquals("hangup", actions.get(3).getApplication());
            assertEquals("NORMAL_CLEARING", actions.get(3).getData());
        }

        // test remove extension
        assertEquals(5, m_openAcdContext.getCommands().size());
        m_openAcdContext.deleteExtension(extensionById);
        assertEquals(4, m_openAcdContext.getCommands().size());
    }

    public void testOpenAcdExtensionAliasProvider() throws Exception {

        OpenAcdLine extension = createOpenAcdLine("sales");
        m_openAcdContextImpl.saveExtension(extension);
        assertEquals(1, m_openAcdContextImpl.getBeanIdsOfObjectsWithAlias("sales").size());
        assertFalse(m_openAcdContextImpl.isAliasInUse("test"));
        assertTrue(m_openAcdContextImpl.isAliasInUse("sales"));
        assertTrue(m_openAcdContextImpl.isAliasInUse("300"));

        SipxFreeswitchService service = new MockSipxFreeswitchService();
        service.setBeanId(SipxFreeswitchService.BEAN_ID);
        service.setLocationsManager(m_locationsManager);

        SipxServiceManager sm = TestHelper.getMockSipxServiceManager(true, service);
        m_openAcdContext.setSipxServiceManager(sm);

        OpenAcdLine extension = DefaultContextConfigurationTest.createOpenAcdLine("sales");

        m_openAcdContext.saveExtension(extension);

        assertEquals(1, m_openAcdContext.getBeanIdsOfObjectsWithAlias("sales").size());
        assertFalse(m_openAcdContext.isAliasInUse("test"));
        assertTrue(m_openAcdContext.isAliasInUse("sales"));
        assertTrue(m_openAcdContext.isAliasInUse("300"));


    }

    public void testOpenAcdAgentGroupCrud() throws Exception {
        // 'Default' agent group
        assertEquals(1, m_openAcdContext.getAgentGroups().size());

        // test get agent group by name
        OpenAcdAgentGroup defaultAgentGroup = m_openAcdContext.getAgentGroupByName("Default");
        assertNotNull(defaultAgentGroup);
        assertEquals("Default", defaultAgentGroup.getName());

        // test save agent group without name
        OpenAcdAgentGroup group = new OpenAcdAgentGroup();
        try {
            m_openAcdContext.saveAgentGroup(group);
            fail();
        } catch (UserException ex) {
        }

        // test save agent group without agents
        assertEquals(1, m_openAcdContext.getAgentGroups().size());
        group.setName("Group");
        group.setDescription("Group description");
        m_openAcdContext.saveAgentGroup(group);
        assertEquals(2, m_openAcdContext.getAgentGroups().size());
        MongoTestCaseHelper.assertObjectWithFieldsValuesPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME}, new String[]{"openacdagentgroup", "Group"});

        // test save agent group with agents
        loadDataSet("common/SampleUsersSeed.xml");
        User alpha = m_coreContext.loadUser(1001);
        OpenAcdAgent supervisor = new OpenAcdAgent();
        supervisor.setGroup(group);
        supervisor.setUser(alpha);
        supervisor.setPin("123456");
        supervisor.setSecurity(OpenAcdAgent.Security.SUPERVISOR.toString());

        User beta = m_coreContext.loadUser(1002);
        OpenAcdAgent agent = new OpenAcdAgent();
        agent.setGroup(group);
        agent.setUser(beta);
        agent.setPin("123457");
        agent.setSecurity(OpenAcdAgent.Security.AGENT.toString());

        assertEquals(0, group.getAgents().size());
        List<OpenAcdAgent> agents = new ArrayList<OpenAcdAgent>(2);
        agents.add(supervisor);
        agents.add(agent);
        m_openAcdContext.addAgentsToGroup(group, agents);
        assertEquals(2, group.getAgents().size());
        MongoTestCaseHelper.assertObjectWithFieldsValuesPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME, MongoConstants.AGENT_GROUP},
                new String[]{"openacdagent", "alpha", "Group"});
        MongoTestCaseHelper.assertObjectWithFieldsValuesPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME, MongoConstants.AGENT_GROUP},
                new String[]{"openacdagent", "beta", "Group"});
        // test save agent group with same name
        try {
            OpenAcdAgentGroup sameAgentGroupName = new OpenAcdAgentGroup();
            sameAgentGroupName.setName("Group");
            m_openAcdContext.saveAgentGroup(sameAgentGroupName);
            fail();
        } catch (UserException ex) {
        }

        // test get agent group by name
        OpenAcdAgentGroup savedAgentGroup = m_openAcdContext.getAgentGroupByName("Group");
        assertNotNull(savedAgentGroup);
        assertEquals("Group", savedAgentGroup.getName());

        // test modify agent group without changing name
        try {
            m_openAcdContext.saveAgentGroup(savedAgentGroup);
        } catch (NameInUseException ex) {
            fail();
        }

        // test get agent group by id
        Integer id = savedAgentGroup.getId();
        OpenAcdAgentGroup agentGroupById = m_openAcdContext.getAgentGroupById(id);
        assertNotNull(agentGroupById);
        assertEquals("Group", agentGroupById.getName());

        // test remove agent groups but prevent 'Default' group deletion
        OpenAcdAgentGroup anotherGroup = new OpenAcdAgentGroup();
        anotherGroup.setName("anotherGroup");
        m_openAcdContext.saveAgentGroup(anotherGroup);
        assertEquals(3, m_openAcdContext.getAgentGroups().size());

        try {
            m_openAcdContext.deleteAgentGroup(defaultAgentGroup);
            fail();
        } catch (DefaultAgentGroupDeleteException e) {
        }
        m_openAcdContext.deleteAgentGroup(group);
        MongoTestCaseHelper.assertObjectWithFieldsValuesNotPresent(getEntityCollection(),
                new String[]{MongoConstants.TYPE, MongoConstants.NAME}, new String[]{"openacdagentgroup", "Group"});
        MongoTestCaseHelper.assertObjectWithFieldsValuesNotPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME, MongoConstants.AGENT_GROUP},
                new String[]{"openacdagent", "alpha", "Group"});
        MongoTestCaseHelper.assertObjectWithFieldsValuesNotPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME, MongoConstants.AGENT_GROUP},
                new String[]{"openacdagent", "beta", "Group"});
        m_openAcdContext.deleteAgentGroup(anotherGroup);
        assertEquals(1, m_openAcdContext.getAgentGroups().size());
    }

    public void testOpenAcdAgentCrud() throws Exception {
        loadDataSet("common/SampleUsersSeed.xml");

        loadDataSetXml("commserver/seedLocations.xml");
        loadDataSetXml("domain/DomainSeed.xml");
        User charlie = m_coreContext.loadUser(1003);

        OpenAcdAgentGroup group = new OpenAcdAgentGroup();
        group.setName("Group");
        m_openAcdContext.saveAgentGroup(group);
        // test save agent
        OpenAcdAgent agent = new OpenAcdAgent();
        assertEquals(0, m_openAcdContext.getAgents().size());
        agent.setGroup(group);
        agent.setUser(charlie);
        agent.setPin("123456");
        m_openAcdContext.saveAgent(agent);
        MongoTestCaseHelper.assertObjectWithFieldsValuesPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME, MongoConstants.AGENT_GROUP},
                new String[]{"openacdagent", "charlie", "Group"});        
        group.addAgent(agent);
        m_openAcdContext.saveAgentGroup(group);
        assertEquals(1, m_openAcdContext.getAgents().size());
        assertEquals(1, group.getAgents().size());
        Set<OpenAcdAgent> agents = group.getAgents();
        OpenAcdAgent savedAgent = DataAccessUtils.singleResult(agents);
        assertEquals(charlie, savedAgent.getUser());
        assertEquals("123456", savedAgent.getPin());
        assertEquals("AGENT", savedAgent.getSecurity());

        // test get agent by id
        Integer id = savedAgent.getId();
        OpenAcdAgent agentGroupById = m_openAcdContext.getAgentById(id);
        assertNotNull(agentGroupById);
        assertEquals(group, agentGroupById.getGroup());
        assertEquals(charlie, agentGroupById.getUser());
        assertEquals("AGENT", agentGroupById.getSecurity());

        // test add agents to group
        User delta = m_coreContext.loadUser(1004);
        User elephant = m_coreContext.loadUser(1005);
        OpenAcdAgentGroup newGroup = new OpenAcdAgentGroup();
        newGroup.setName("NewGroup");
        OpenAcdAgent agent1 = new OpenAcdAgent();
        agent1.setGroup(newGroup);
        agent1.setPin("1234");
        agent1.setUser(delta);
        OpenAcdAgent agent2 = new OpenAcdAgent();
        agent2.setGroup(newGroup);
        agent2.setPin("123433");
        agent2.setUser(elephant);
        newGroup.addAgent(agent1);
        newGroup.addAgent(agent2);
        m_openAcdContext.saveAgentGroup(newGroup);

        OpenAcdAgentGroup grp = m_openAcdContext.getAgentGroupByName("NewGroup");
        assertEquals(2, grp.getAgents().size());
        assertEquals(3, m_openAcdContext.getAgents().size());

        // test remove agents from group
        grp.removeAgent(agent1);
        m_openAcdContext.saveAgentGroup(grp);
        assertEquals(1, grp.getAgents().size());
        
         * here it should really be 3, but merging the group will cascade into the agent being deleted.
         * maybe this should be fixed, but, the only place this is used is when an agent is deleted.  
         
        assertEquals(2, m_openAcdContext.getAgents().size());
        //Also in this scenario, agent is not removed from mongo
        //MongoTestCaseHelper.assertObjectWithFieldsValuesNotPresent(getEntityCollection(), 
        //        new String[]{MongoConstants.TYPE, MongoConstants.NAME},
        //        new String[]{"openacdagent", "delta"});  

        // remove agents
        m_openAcdContext.deleteAgent(agent2);
        assertEquals(1, m_openAcdContext.getAgents().size());
        MongoTestCaseHelper.assertObjectWithFieldsValuesNotPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME},
                new String[]{"openacdagent", "elephant"}); 

        // remove user should remove also associated agent
        User newAgent = m_coreContext.newUser();
        newAgent.setUserName("test");
        m_coreContext.saveUser(newAgent);
        OpenAcdAgent agent3 = new OpenAcdAgent();
        agent3.setGroup(newGroup);
        agent3.setPin("123433");
        agent3.setUser(newAgent);
        m_openAcdContext.addAgentsToGroup(newGroup, Collections.singletonList(agent3));
        assertEquals(2, m_openAcdContext.getAgents().size());
        m_coreContext.deleteUser(newAgent);
        assertEquals(1, m_openAcdContext.getAgents().size());

        // remove groups
        Collection<Integer> agentGroupIds = new ArrayList<Integer>();
        agentGroupIds.add(group.getId());
        agentGroupIds.add(newGroup.getId());
        m_openAcdContext.deleteAgentGroup(group);
        m_openAcdContext.deleteAgentGroup(newGroup);
        assertEquals(1, m_openAcdContext.getAgentGroups().size());
    }

    public void testOpenAcdSkillGroupCrud() throws Exception {
        // test existing 'Language' and 'Magic' skill groups
        assertEquals(2, m_openAcdContext.getSkillGroups().size());

        // test get skill group by name
        OpenAcdSkillGroup magicSkillGroup = m_openAcdContext.getSkillGroupByName("Magic");
        assertNotNull(magicSkillGroup);
        assertEquals("Magic", magicSkillGroup.getName());
        OpenAcdSkillGroup languageSkillGroup = m_openAcdContext.getSkillGroupByName("Language");
        assertNotNull(languageSkillGroup);
        assertEquals("Language", languageSkillGroup.getName());

        // test save skill group without name
        OpenAcdSkillGroup group = new OpenAcdSkillGroup();
        try {
            m_openAcdContext.saveSkillGroup(group);
            fail();
        } catch (UserException ex) {
        }

        // test save skill group
        assertEquals(2, m_openAcdContext.getSkillGroups().size());
        group.setName("MySkillGroup");
        m_openAcdContext.saveSkillGroup(group);
        assertEquals(3, m_openAcdContext.getSkillGroups().size());

        // test save queue group with the same name
        OpenAcdSkillGroup anotherGroup = new OpenAcdSkillGroup();
        anotherGroup.setName("MySkillGroup");
        try {
            m_openAcdContext.saveSkillGroup(anotherGroup);
            fail();
        } catch (UserException ex) {
        }

        // test get skill group by id
        OpenAcdSkillGroup existingSkillGroup = m_openAcdContext.getSkillGroupById(group.getId());
        assertNotNull(existingSkillGroup);
        assertEquals("MySkillGroup", existingSkillGroup.getName());

        // test remove skill group but prevent 'Magic' skill group deletion
        assertEquals(3, m_openAcdContext.getSkillGroups().size());
        Collection<Integer> ids = new ArrayList<Integer>();
        ids.add(magicSkillGroup.getId());
        ids.add(languageSkillGroup.getId());
        ids.add(group.getId());
        m_openAcdContext.removeSkillGroups(ids);
    }

    public void testOpenAcdSkillCrud() throws Exception {
        // test existing skills in 'Language' and 'Magic' skill groups
        assertEquals(8, m_openAcdContext.getSkills().size());

        // test default skills existing in 'Magic' skill group
        assertEquals(6, m_openAcdContext.getDefaultSkills().size());

        // test default skills for 'default_queue' queue
        OpenAcdQueue defaultQueue = m_openAcdContext.getQueueByName("default_queue");
        assertEquals(2, defaultQueue.getSkills().size());
        List<String> skillNames = new ArrayList<String>();
        for (OpenAcdSkill skill : defaultQueue.getSkills()) {
            skillNames.add(skill.getName());
        }
        assertTrue(skillNames.contains("English"));
        assertTrue(skillNames.contains("Node"));

        // test get skills ordered by group name
        Map<String, List<OpenAcdSkill>> groupedSkills = m_openAcdContext.getGroupedSkills();
        assertTrue(groupedSkills.keySet().contains("Language"));
        assertTrue(groupedSkills.keySet().contains("Magic"));
        assertEquals(2, groupedSkills.get("Language").size());
        assertEquals(6, groupedSkills.get("Magic").size());
        // test get skill by name
        OpenAcdSkill englishSkill = m_openAcdContext.getSkillByName("English");
        assertNotNull(englishSkill);
        assertEquals("English", englishSkill.getName());
        assertEquals("Language", englishSkill.getGroupName());
        assertFalse(englishSkill.isDefaultSkill());

        OpenAcdSkill allSkill = m_openAcdContext.getSkillByName("All");
        assertNotNull(allSkill);
        assertEquals("All", allSkill.getName());
        assertEquals("Magic", allSkill.getGroupName());
        assertTrue(allSkill.isDefaultSkill());

        // test get skill by id
        Integer id = allSkill.getId();
        OpenAcdSkill skillById = m_openAcdContext.getSkillById(id);
        assertNotNull(skillById);
        assertEquals("All", skillById.getName());
        assertEquals("Magic", skillById.getGroupName());

        // test get skill by atom
        String atom = allSkill.getAtom();
        OpenAcdSkill skillByAtom = m_openAcdContext.getSkillByAtom(atom);
        assertNotNull(skillByAtom);
        assertEquals("_all", skillByAtom.getAtom());
        assertEquals("All", skillByAtom.getName());
        assertEquals("Magic", skillByAtom.getGroupName());

        // test save skill without name
        OpenAcdSkill newSkill = new OpenAcdSkill();
        try {
            m_openAcdContext.saveSkill(newSkill);
            fail();
        } catch (UserException ex) {
        }
        // test save skill without atom
        newSkill.setName("TestSkill");
        try {
            m_openAcdContext.saveSkill(newSkill);
            fail();
        } catch (UserException ex) {
        }
        // test save skill without group
        newSkill.setAtom("_atom");
        try {
            m_openAcdContext.saveSkill(newSkill);
            fail();
        } catch (UserException ex) {
        }

        // test save skill
        OpenAcdSkillGroup skillGroup = new OpenAcdSkillGroup();
        skillGroup.setName("Group");
        m_openAcdContext.saveSkillGroup(skillGroup);
        newSkill.setGroup(skillGroup);

        assertEquals(8, m_openAcdContext.getSkills().size());
        m_openAcdContext.saveSkill(newSkill);
        assertEquals(9, m_openAcdContext.getSkills().size());
        MongoTestCaseHelper.assertObjectWithFieldsValuesPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME, MongoConstants.GROUP_NAME},
                new String[]{"openacdskill", "TestSkill", "Group"});

        // test remove skills but prevent 'magic' skills deletion
        assertEquals(9, m_openAcdContext.getSkills().size());
        try {
            m_openAcdContext.deleteSkill(allSkill);
            fail();
        } catch (SkillInUseException e) {
        }
        m_openAcdContext.deleteSkill(newSkill);
        assertEquals(8, m_openAcdContext.getSkills().size());
        MongoTestCaseHelper.assertObjectWithFieldsValuesNotPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME, MongoConstants.AGENT_GROUP},
                new String[]{"openacdskill", "TestSkill", "Group"});
        
        OpenAcdSkill newSkill1 = new OpenAcdSkill();
        newSkill1.setName("TestSkill1");
        newSkill1.setAtom("_atom");
        newSkill1.setGroup(skillGroup);
        m_openAcdContext.saveSkill(newSkill1);
        MongoTestCaseHelper.assertObjectWithFieldsValuesPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME, MongoConstants.GROUP_NAME},
                new String[]{"openacdskill", "TestSkill1", "Group"});
        m_openAcdContext.removeSkillGroups(Collections.singletonList(skillGroup.getId()));
        MongoTestCaseHelper.assertObjectWithFieldsValuesNotPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME, MongoConstants.AGENT_GROUP},
                new String[]{"openacdskill", "TestSkill1", "Group"});
        
    }

    public void testManageAgentGroupWithSkill() throws Exception {
        OpenAcdSkill englishSkill = m_openAcdContext.getSkillByName("English");
        OpenAcdSkill germanSkill = m_openAcdContext.getSkillByName("German");
        OpenAcdAgentGroup group = new OpenAcdAgentGroup();
        group.setName("Group");
        group.addSkill(englishSkill);
        m_openAcdContext.saveAgentGroup(group);
        assertTrue(m_openAcdContext.getAgentGroupByName("Group").getSkills().contains(englishSkill));

        // cannot delete assigned skill
        assertEquals(8, m_openAcdContext.getSkills().size());
        try {
            m_openAcdContext.deleteSkill(englishSkill);
            fail();
        }
        catch (SkillInUseException e) {
        }
        m_openAcdContext.deleteSkill(germanSkill);
        assertEquals(7, m_openAcdContext.getSkills().size());
    }

    public void testManageAgentWithSkill() throws Exception {
        loadDataSet("common/SampleUsersSeed.xml");
        User charlie = m_coreContext.loadUser(1003);
        OpenAcdSkill englishSkill = m_openAcdContext.getSkillByName("English");
        OpenAcdSkill germanSkill = m_openAcdContext.getSkillByName("German");
        OpenAcdAgentGroup group = new OpenAcdAgentGroup();
        group.setName("Group");
        m_openAcdContext.saveAgentGroup(group);

        OpenAcdAgent agent = new OpenAcdAgent();
        agent.setGroup(group);
        agent.setUser(charlie);
        agent.setPin("123456");
        agent.addSkill(englishSkill);
        m_openAcdContext.saveAgent(agent);
        assertTrue(m_openAcdContext.getAgentByUser(charlie).getSkills().contains(englishSkill));

        // cannot delete assigned skill
        assertEquals(8, m_openAcdContext.getSkills().size());
        // cannot delete assigned skill
        try {
            m_openAcdContext.deleteSkill(englishSkill);
            fail();
        }
        catch (SkillInUseException e) {
        }
        m_openAcdContext.deleteSkill(germanSkill);
        assertEquals(7, m_openAcdContext.getSkills().size());
    }

    public void testManageQueueGroupWithSkill() throws Exception {
        OpenAcdSkill englishSkill = m_openAcdContext.getSkillByName("English");
        OpenAcdSkill germanSkill = m_openAcdContext.getSkillByName("German");
        OpenAcdQueueGroup queueGroup = new OpenAcdQueueGroup();
        queueGroup.setName("QGroup");
        queueGroup.addSkill(englishSkill);
        m_openAcdContext.saveQueueGroup(queueGroup);
        assertTrue(m_openAcdContext.getQueueGroupByName("QGroup").getSkills().contains(englishSkill));

        // cannot delete assigned skill
        assertEquals(8, m_openAcdContext.getSkills().size());
        try {
            m_openAcdContext.deleteSkill(englishSkill);
            fail();
        }
        catch (SkillInUseException e) {
        }
        m_openAcdContext.deleteSkill(germanSkill);
        assertEquals(7, m_openAcdContext.getSkills().size());
    }

    public void testManageQueueWithSkill() throws Exception {
        OpenAcdSkill englishSkill = m_openAcdContext.getSkillByName("English");
        OpenAcdSkill germanSkill = m_openAcdContext.getSkillByName("German");
        OpenAcdQueueGroup defaultQueueGroup = m_openAcdContext.getQueueGroupByName("Default");
        assertNotNull(defaultQueueGroup);
        OpenAcdQueue queue = new OpenAcdQueue();
        queue.setName("Queue");
        queue.setGroup(defaultQueueGroup);
        queue.addSkill(englishSkill);
        m_openAcdContext.saveQueue(queue);
        assertTrue(m_openAcdContext.getQueueByName("Queue").getSkills().contains(englishSkill));

        // cannot delete assigned skill
        assertEquals(8, m_openAcdContext.getSkills().size());
        try {
            m_openAcdContext.deleteSkill(englishSkill);
            fail();
        }
        catch (SkillInUseException e) {
        }
        m_openAcdContext.deleteSkill(germanSkill);
        assertEquals(7, m_openAcdContext.getSkills().size());
    }

    public void testGetSkillsAtoms() {
        OpenAcdSkill skill1 = new OpenAcdSkill();
        skill1.setName("Skill1");
        skill1.setAtom("_skill1");
        OpenAcdSkill skill2 = new OpenAcdSkill();
        skill2.setName("Skill2");
        skill2.setAtom("_skill2");
        OpenAcdAgentGroup group = new OpenAcdAgentGroup();
        group.addSkill(skill1);
        group.addSkill(skill2);
        OpenAcdAgent agent = new OpenAcdAgent();
        agent.addSkill(skill1);
        agent.addSkill(skill2);
        assertEquals("_skill1, _skill2", group.getSkillsAtoms());
        assertEquals("_skill1, _skill2", agent.getSkillsAtoms());
    }

    public void testGetGroupedSkills() throws Exception {
        OpenAcdSkillGroup skillGroup = new OpenAcdSkillGroup();
        skillGroup.setName("NewGroup");
        m_openAcdContext.saveSkillGroup(skillGroup);

        OpenAcdSkill newSkill = new OpenAcdSkill();
        newSkill.setName("NewSkill");
        newSkill.setAtom("_new");
        newSkill.setGroup(skillGroup);
        m_openAcdContext.saveSkill(newSkill);

        OpenAcdSkill anotherSkill = new OpenAcdSkill();
        anotherSkill.setName("AnotherSkill");
        anotherSkill.setAtom("_another");
        anotherSkill.setGroup(skillGroup);
        m_openAcdContext.saveSkill(anotherSkill);

        OpenAcdSkillGroup anotherSkillGroup = new OpenAcdSkillGroup();
        anotherSkillGroup.setName("AnotherSkillGroup");
        m_openAcdContext.saveSkillGroup(anotherSkillGroup);

        OpenAcdSkill thirdSkill = new OpenAcdSkill();
        thirdSkill.setName("ThirdSkill");
        thirdSkill.setAtom("_third");
        thirdSkill.setGroup(anotherSkillGroup);
        m_openAcdContext.saveSkill(thirdSkill);

        Map<String, List<OpenAcdSkill>> skills = m_openAcdContext.getGroupedSkills();
        assertEquals(2, skills.get("NewGroup").size());
        assertTrue(skills.get("NewGroup").contains(m_openAcdContext.getSkillByAtom("_new")));
        assertTrue(skills.get("NewGroup").contains(m_openAcdContext.getSkillByAtom("_another")));
        assertEquals(1, skills.get("AnotherSkillGroup").size());
        assertTrue(skills.get("AnotherSkillGroup").contains(m_openAcdContext.getSkillByAtom("_third")));
        assertEquals(2, skills.get("Language").size());
        assertEquals(6, skills.get("Magic").size());
    }

    public void testOpenAcdClient() throws Exception {
        // get 'Demo Client' client
        assertEquals(1, m_openAcdContext.getClients().size());

        // test get client by name
        OpenAcdClient defaultClient = m_openAcdContext.getClientByName("Demo Client");
        assertNotNull(defaultClient);
        assertEquals("Demo Client", defaultClient.getName());
        assertEquals("00990099", defaultClient.getIdentity());

        // test save client
        OpenAcdClient client = new OpenAcdClient();
        client.setName("client");
        client.setIdentity("10101");
        m_openAcdContext.saveClient(client);
        assertEquals(2, m_openAcdContext.getClients().size());

        // test save client with the same name
        OpenAcdClient anotherClient = new OpenAcdClient();
        anotherClient.setName("client");
        try {
            m_openAcdContext.saveClient(anotherClient);
            fail();
        } catch (UserException ex) {
        }

        // test save client with the same identity
        anotherClient.setName("anotherClient");
        anotherClient.setIdentity("10101");
        try {
            m_openAcdContext.saveClient(anotherClient);
            fail();
        } catch (UserException ex) {
        }
        anotherClient.setIdentity("11111");
        m_openAcdContext.saveClient(anotherClient);
        assertEquals(3, m_openAcdContext.getClients().size());
        MongoTestCaseHelper.assertObjectWithFieldsValuesPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME},
                new String[]{"openacdclient", "client"});
        // test remove clients but prevent 'Demo Client' deletion
        try {
            m_openAcdContext.deleteClient(defaultClient);
            fail();
        } catch (ClientInUseException e) {
        }
        m_openAcdContext.deleteClient(client);
        MongoTestCaseHelper.assertObjectWithFieldsValuesNotPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME},
                new String[]{"openacdclient", "client"});
        m_openAcdContext.deleteClient(anotherClient);
        assertEquals(1, m_openAcdContext.getClients().size());
        assertNotNull(m_openAcdContext.getClientByName("Demo Client"));
        assertEquals("Demo Client", m_openAcdContext.getClientById(defaultClient.getId()).getName());
        assertEquals("Demo Client", m_openAcdContext.getClientByIdentity(defaultClient.getIdentity()).getName());
    }

    public void testOpenAcdQueueGroupCrud() throws Exception {
        // 'Default' queue group
        assertEquals(1, m_openAcdContext.getQueueGroups().size());

        // test get queue group by name
        OpenAcdQueueGroup defaultQueueGroup = m_openAcdContext.getQueueGroupByName("Default");
        assertNotNull(defaultQueueGroup);
        assertEquals("Default", defaultQueueGroup.getName());

        // test save queue group without name
        OpenAcdQueueGroup group = new OpenAcdQueueGroup();
        try {
            m_openAcdContext.saveQueueGroup(group);
            fail();
        } catch (UserException ex) {
        }

        // test save queue group
        group.setName("QueueGroup");
        m_openAcdContext.saveQueueGroup(group);
        assertEquals(2, m_openAcdContext.getQueueGroups().size());
        MongoTestCaseHelper.assertObjectWithFieldsValuesPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME},
                new String[]{"openacdqueuegroup", "QueueGroup"});
        
        // test get queue group by id
        OpenAcdQueueGroup existingQueueGroup = m_openAcdContext.getQueueGroupById(group.getId());
        assertNotNull(existingQueueGroup);
        assertEquals("QueueGroup", existingQueueGroup.getName());

        // test save queue group with the same name
        OpenAcdQueueGroup anotherGroup = new OpenAcdQueueGroup();
        anotherGroup.setName("QueueGroup");
        try {
            m_openAcdContext.saveQueueGroup(anotherGroup);
            fail();
        } catch (UserException ex) {
        }

        // test save queue group with skills
        assertEquals(2, m_openAcdContext.getQueueGroups().size());
        Set<OpenAcdSkill> skills = new LinkedHashSet<OpenAcdSkill>();
        OpenAcdSkill englishSkill = m_openAcdContext.getSkillByName("English");
        OpenAcdSkill germanSkill = m_openAcdContext.getSkillByName("German");
        skills.add(englishSkill);
        skills.add(germanSkill);
        OpenAcdQueueGroup groupWithSkills = new OpenAcdQueueGroup();
        groupWithSkills.setName("QueueGroupWithSkills");
        groupWithSkills.setSkills(skills);
        m_openAcdContext.saveQueueGroup(groupWithSkills);
        assertEquals(3, m_openAcdContext.getQueueGroups().size());

        // test remove queue group but prevent 'Default' queue group deletion
        Collection<Integer> queueGroupIds = new ArrayList<Integer>();
        queueGroupIds.add(defaultQueueGroup.getId());
        queueGroupIds.add(group.getId());
        queueGroupIds.add(groupWithSkills.getId());
        try {
            m_openAcdContext.deleteQueueGroup(defaultQueueGroup);
            fail();
        } catch (QueueGroupInUseException e) {
        }
        m_openAcdContext.deleteQueueGroup(group);
        MongoTestCaseHelper.assertObjectWithFieldsValuesNotPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME},
                new String[]{"openacdqueuegroup", "QueueGroup"});        
        m_openAcdContext.deleteQueueGroup(groupWithSkills);
        assertEquals(1, m_openAcdContext.getQueueGroups().size());
        assertNotNull(m_openAcdContext.getQueueGroupByName("Default"));
    }

    public void testOpenAcdQueueCrud() throws Exception {
        // get 'default_queue' queue
        assertEquals(1, m_openAcdContext.getQueues().size());

        // test get queue by name
        OpenAcdQueue defaultQueue = m_openAcdContext.getQueueByName("default_queue");
        assertNotNull(defaultQueue);
        assertEquals("default_queue", defaultQueue.getName());
        assertEquals("Default", defaultQueue.getGroup().getName());

        // test save queue without name
        OpenAcdQueue queue = new OpenAcdQueue();
        try {
            m_openAcdContext.saveQueue(queue);
            fail();
        } catch (UserException ex) {
        }

        // test save queue
        OpenAcdQueueGroup defaultQueueGroup = m_openAcdContext.getQueueGroupByName("Default");
        assertNotNull(defaultQueueGroup);
        queue.setName("Queue1");
        queue.setGroup(defaultQueueGroup);
        m_openAcdContext.saveQueue(queue);
        assertEquals(2, m_openAcdContext.getQueues().size());
        MongoTestCaseHelper.assertObjectWithFieldsValuesPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME},
                new String[]{"openacdqueue", "Queue1"});        

        // test get queue by id
        OpenAcdQueue existingQueue = m_openAcdContext.getQueueById(queue.getId());
        assertNotNull(existingQueue);
        assertEquals("Queue1", existingQueue.getName());
        assertEquals("Default", existingQueue.getGroup().getName());

        // test save queue group with the same name
        OpenAcdQueue anotherQueue = new OpenAcdQueue();
        anotherQueue.setName("Queue1");
        try {
            m_openAcdContext.saveQueue(anotherQueue);
            fail();
        } catch (UserException ex) {
        }

        // test save queue with skills
        assertEquals(2, m_openAcdContext.getQueues().size());
        Set<OpenAcdSkill> skills = new LinkedHashSet<OpenAcdSkill>();
        OpenAcdSkill englishSkill = m_openAcdContext.getSkillByName("English");
        OpenAcdSkill germanSkill = m_openAcdContext.getSkillByName("German");
        skills.add(englishSkill);
        skills.add(germanSkill);
        OpenAcdQueue queueWithSkills = new OpenAcdQueue();
        queueWithSkills.setName("QueueWithSkills");
        queueWithSkills.setGroup(defaultQueueGroup);
        queueWithSkills.setSkills(skills);
        m_openAcdContext.saveQueue(queueWithSkills);
        assertEquals(3, m_openAcdContext.getQueues().size());

        // test save queue with recipe steps
        OpenAcdRecipeAction recipeAction1 = new OpenAcdRecipeAction();
        recipeAction1.setAction(ACTION.SET_PRIORITY.toString());
        recipeAction1.setActionValue("1");

        OpenAcdRecipeCondition recipeCondition1 = new OpenAcdRecipeCondition();
        recipeCondition1.setCondition(CONDITION.TICK_INTERVAL.toString());
        recipeCondition1.setRelation("is");
        recipeCondition1.setValueCondition("6");

        OpenAcdRecipeStep recipeStep1 = new OpenAcdRecipeStep();
        recipeStep1.setName("RecipeStep1");
        recipeStep1.setFrequency(FREQUENCY.RUN_ONCE.toString());
        recipeStep1.setAction(recipeAction1);
        recipeStep1.addCondition(recipeCondition1);

        OpenAcdRecipeAction recipeAction2 = new OpenAcdRecipeAction();
        recipeAction2.setAction(ACTION.PRIORITIZE.toString());

        OpenAcdRecipeCondition recipeCondition2 = new OpenAcdRecipeCondition();
        recipeCondition2.setCondition(CONDITION.CALLS_IN_QUEUE.toString());
        recipeCondition2.setRelation("less");
        recipeCondition2.setValueCondition("10");

        OpenAcdRecipeStep recipeStep2 = new OpenAcdRecipeStep();
        recipeStep2.setName("RecipeStep2");
        recipeStep2.setFrequency(FREQUENCY.RUN_MANY.toString());
        recipeStep2.setAction(recipeAction2);
        recipeStep2.addCondition(recipeCondition1);
        recipeStep2.addCondition(recipeCondition2);

        OpenAcdQueue queueWithRecipeStep = new OpenAcdQueue();
        queueWithRecipeStep.setName("QueueRecipe");
        queueWithRecipeStep.setGroup(defaultQueueGroup);
        queueWithRecipeStep.addStep(recipeStep1);
        queueWithRecipeStep.addStep(recipeStep2);
        m_openAcdContext.saveQueue(queueWithRecipeStep);

        assertEquals(4, m_openAcdContext.getQueues().size());
        OpenAcdQueue queueRecipe = m_openAcdContext.getQueueByName("QueueRecipe");
        assertNotNull(queueRecipe);
        assertEquals(2, queueRecipe.getSteps().size());

        // test remove step from queue
        queueRecipe.removeStep(recipeStep1);
        m_openAcdContext.saveQueue(queueWithRecipeStep);
        assertEquals(1, queueRecipe.getSteps().size());

        // test remove queue with recipe steps
        m_openAcdContext.deleteQueue(queueWithRecipeStep);
        assertEquals(3, m_openAcdContext.getQueues().size());

        // test remove all queues but prevent 'default_queue' deletion
        Collection<Integer> queueIds = new ArrayList<Integer>();
        queueIds.add(defaultQueue.getId());
        queueIds.add(queue.getId());
        queueIds.add(queueWithSkills.getId());
        try {
            m_openAcdContext.deleteQueue(defaultQueue);    
            fail();
        } catch (QueueInUseException e) {
        }
        m_openAcdContext.deleteQueue(queue);
        MongoTestCaseHelper.assertObjectWithFieldsValuesNotPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME},
                new String[]{"openacdqueue", "Queue1"});  
        m_openAcdContext.deleteQueue(queueWithSkills);
        assertEquals(1, m_openAcdContext.getQueues().size());
        
        OpenAcdQueueGroup group = new OpenAcdQueueGroup();
        group.setName("QueueGroup");
        group.addQueue(anotherQueue);
        anotherQueue.setName("q2");
        anotherQueue.setGroup(group);
        m_openAcdContext.saveQueueGroup(group);
        m_openAcdContext.saveQueue(anotherQueue);
        MongoTestCaseHelper.assertObjectWithFieldsValuesPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME},
                new String[]{"openacdqueue", "q2"}); 
        m_openAcdContext.deleteQueueGroup(group);
        MongoTestCaseHelper.assertObjectWithFieldsValuesNotPresent(getEntityCollection(), 
                new String[]{MongoConstants.TYPE, MongoConstants.NAME},
                new String[]{"openacdqueue", "q2"}); 
    }

    public void testGroupedSkills() throws Exception {
        OpenAcdSkill brand = m_openAcdContext.getSkillByAtom("_brand");
        OpenAcdSkill agent = m_openAcdContext.getSkillByAtom("_agent");
        OpenAcdSkill profile = m_openAcdContext.getSkillByAtom("_profile");
        OpenAcdSkill node = m_openAcdContext.getSkillByAtom("_node");
        OpenAcdSkill queue = m_openAcdContext.getSkillByAtom("_queue");
        OpenAcdSkill all = m_openAcdContext.getSkillByAtom("_all");
        List<OpenAcdSkill> agentSkills = m_openAcdContext.getAgentGroupedSkills().get("Magic");
        assertFalse(agentSkills.contains(brand));
        assertTrue(agentSkills.contains(agent));
        assertTrue(agentSkills.contains(profile));
        assertTrue(agentSkills.contains(node));
        assertFalse(agentSkills.contains(queue));
        assertTrue(agentSkills.contains(all));
        List<OpenAcdSkill> queueSkills = m_openAcdContext.getQueueGroupedSkills().get("Magic");
        assertTrue(queueSkills.contains(brand));
        assertFalse(queueSkills.contains(agent));
        assertFalse(queueSkills.contains(profile));
        assertTrue(queueSkills.contains(node));
        assertTrue(queueSkills.contains(queue));
        assertTrue(queueSkills.contains(all));
    }

    public void setOpenAcdContext(OpenAcdContext openAcdContext) {
        m_openAcdContext = openAcdContext;
    }

    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    public void setLocationsManager(LocationsManager locationsManager) {
        m_locationsManager = locationsManager;
    }

    public void setOpenAcdSkillGroupMigrationContext(OpenAcdSkillGroupMigrationContext migrationContext) {
        m_migrationContext = migrationContext;
    }*/
}