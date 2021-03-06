<%--
  ~ Copyright 2005-2006 The Apache Software Foundation.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ taglib prefix="ww" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<ww:i18n name="org.codehaus.plexus.redback.xwork.default">
<head>
  <title><ww:text name="create.admin.page.title"/></title>
</head>

<body>

<c:import url="/WEB-INF/jsp/redback/include/formValidationResults.jsp" />

<h2><ww:text name="create.admin.section.title"/></h2>

<ww:form action="addadmin!submit" namespace="/security" theme="xhtml"
         id="adminCreateForm" method="post" name="admincreate" cssClass="security adminCreate">
  <c:import url="/WEB-INF/jsp/redback/include/userCredentials.jsp" />
  <ww:submit value="%{getText('create.admin')}" />
</ww:form>

</body>
</ww:i18n>
</html>
