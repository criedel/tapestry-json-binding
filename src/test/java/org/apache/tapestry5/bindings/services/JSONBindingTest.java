// Copyright 2012 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.bindings.services;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.BindingFactory;
import org.apache.tapestry5.test.PageTester;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class JSONBindingTest extends TapestryTestCase {

    private PageTester pageTester;

    private BindingFactory factory;

    @BeforeClass
    public void setup_factory() {

        pageTester = new PageTester("org.apache.tapestry5.bindings", "JSONBinding");
        this.factory = pageTester.getRegistry().getService("JSONBindingFactory", BindingFactory.class);
    }

    @AfterClass
    public void cleanup_factory() {

        factory = null;
    }

    private ComponentResources newComponentResources(Component component)
    {
        ComponentResources resources = mockComponentResources();
        train_getComponent(resources, component);

        train_getCompleteId(resources, "foo.Bar:baz");

        return resources;
    }

    @Test
    public void jsonobject_property() {

        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "jsonObject", l);

        assertSame(binding.getBindingType(), JSONObject.class);

        bean.setJsonObject(new JSONObject("name", "amelie"));

        assertEquals(((JSONObject) binding.get()).get("name"), "amelie");

        binding.set(new JSONObject("name", "cathy"));

        assertEquals(bean.getJsonObject().get("name"), "cathy");
        assertEquals(InternalUtils.locationOf(binding), l);

        assertEquals(binding.toString(), "PropBinding[test binding foo.Bar:baz(jsonObject)]");

        verify();
    }

    @Test
    public void jsonarray_property() {

        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "jsonObject.names", l);
        final JSONObject jsonObject = new JSONObject();
        final JSONArray names = new JSONArray("amelie", "cathy");
        jsonObject.put("names", names);

        bean.setJsonObject(jsonObject);

        assertSame(binding.getBindingType(), JSONArray.class);

        assertEquals(binding.get(), names);

        names.put("eva");
        assertEquals(binding.get(), names);

        assertEquals(InternalUtils.locationOf(binding), l);

        assertEquals(binding.toString(), "JSONBinding[test binding foo.Bar:baz(jsonObject.names)]");

        verify();
    }

    @Test
    public void jsonarray_access() {

        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "jsonObject.names[1]", l);
        final JSONObject jsonObject = new JSONObject();
        final JSONArray names = new JSONArray("amelie", "cathy");
        jsonObject.put("names", names);

        bean.setJsonObject(jsonObject);

        assertSame(binding.getBindingType(), String.class);

        assertEquals(binding.get(), "cathy");

        assertEquals(InternalUtils.locationOf(binding), l);

        assertEquals(binding.toString(), "JSONBinding[test binding foo.Bar:baz(jsonObject.names[1])]");

        verify();
    }

    @Test
    public void jsonobject_guarded_null_access() {

        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "jsonObject.user?.name", l);
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("user", new JSONObject());

        bean.setJsonObject(jsonObject);

        assertEquals(binding.get(), null);

        assertEquals(InternalUtils.locationOf(binding), l);

        assertEquals(binding.toString(), "JSONBinding[test binding foo.Bar:baz(jsonObject.user?.name)]");

        verify();
    }

    @Test
    public void jsonobject_in_array_with_guarded_null_access() {

        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "jsonObject.users[0]?.NAME", l);

        final JSONObject jsonObject = new JSONObject();
        final JSONArray users = new JSONArray(new JSONObject("name", "nobody"));
        jsonObject.put("users", users);

        bean.setJsonObject(jsonObject);

        assertEquals(binding.get(), null);

        assertEquals(InternalUtils.locationOf(binding), l);

        assertEquals(binding.toString(), "JSONBinding[test binding foo.Bar:baz(jsonObject.users[0]?.NAME)]");

        verify();
    }

    @Test
    public void nested_jsonarray_access() {

        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "jsonObject.nested.names[1]", l);
        final JSONObject jsonObject = new JSONObject();
        final JSONObject nestedJsonObject = new JSONObject();

        final JSONArray names = new JSONArray("amelie", "cathy");
        jsonObject.put("names", names);

        nestedJsonObject.put("nested", jsonObject);

        bean.setJsonObject(nestedJsonObject);

        assertSame(binding.getBindingType(), String.class);

        assertEquals(binding.get(), "cathy");

        assertEquals(InternalUtils.locationOf(binding), l);

        assertEquals(binding.toString(), "JSONBinding[test binding foo.Bar:baz(jsonObject.nested.names[1])]");

        verify();
    }

    @Test
    public void nested_jsonarray_with_nested_property_access() {

        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "jsonObject.nested.users[1].name", l);
        final JSONObject jsonObject = new JSONObject();
        final JSONObject nestedJsonObject = new JSONObject();


        final JSONArray users = new JSONArray(new JSONObject("name", "amelie"),
                                              new JSONObject("name", "cathy"));

        jsonObject.put("users", users);

        nestedJsonObject.put("nested", jsonObject);

        bean.setJsonObject(nestedJsonObject);

        assertSame(binding.getBindingType(), String.class);

        assertEquals(binding.get(), "cathy");

        assertEquals(InternalUtils.locationOf(binding), l);

        assertEquals(binding.toString(), "JSONBinding[test binding foo.Bar:baz(jsonObject.nested.users[1].name)]");

        verify();
    }

    @Test
    public void firstclass_jsonarray_with_nested_property_access() {

        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "jsonArray[1].name", l);

        final JSONArray users = new JSONArray(new JSONObject("name", "amelie"),
                                              new JSONObject("name", "cathy"));

        bean.setJsonArray(users);

        assertSame(binding.getBindingType(), String.class);

        assertEquals(binding.get(), "cathy");

        assertEquals(InternalUtils.locationOf(binding), l);

        assertEquals(binding.toString(), "JSONBinding[test binding foo.Bar:baz(jsonArray[1].name)]");

        verify();
    }

    @Test
    public void render_jsonbindings() {

        final Document document = pageTester.renderPage("BindingDemo");

        final String documentString = document.toString();
        assertTrue(documentString.contains("amelie"), "Rendered instead: " + documentString);
        assertTrue(documentString.contains("cathy"), "Rendered instead: " + documentString);

        assertTrue(documentString.contains("jeff"), "Rendered instead: " + documentString);
        assertTrue(documentString.contains("rio"), "Rendered instead: " + documentString);

        assertTrue(documentString.contains("this is empty:()"), "Rendered instead: " + documentString);
    }
}
