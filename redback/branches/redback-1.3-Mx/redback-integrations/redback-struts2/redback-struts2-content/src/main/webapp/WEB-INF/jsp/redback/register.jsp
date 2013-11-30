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

<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<s:i18n name="org.codehaus.plexus.redback.struts2.default">
<head>
  <title><s:text name="register.page.title"/></title>
</head>

<body>

<%@ include file="/WEB-INF/jsp/redback/include/formValidationResults.jsp" %>

<h2><s:text name="register.section.title"/></h2>
   
<s:form action="register_submit" namespace="/security" theme="xhtml"
         id="registerForm" method="post" name="register" cssClass="security register">     
  
  <s:textfield label="%{getText('username')}"         name="user.username" size="30" requiredLabel="true"/>
  <s:textfield label="%{getText('full.name')}"        name="user.fullName" size="30" requiredLabel="true"/>
  <s:textfield label="%{getText('email.address')}"    name="user.email" size="50"    requiredLabel="true"/>

  <c:if test="${! emailValidationRequired}">
    <s:password  label="%{getText('password')}"         name="user.password" size="20" requiredLabel="true"/>
    <s:password  label="%{getText('confirm.password')}" name="user.confirmPassword" size="20" requiredLabel="true"/>
  </c:if>
  
  <s:submit value="%{getText('register')}" />
  <s:submit value="%{getText('cancel')}" name="cancel" />
</s:form>

</body>
</s:i18n>
</html>
