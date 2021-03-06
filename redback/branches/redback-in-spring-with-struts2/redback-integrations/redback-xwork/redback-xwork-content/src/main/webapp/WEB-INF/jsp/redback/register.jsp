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

<%@ taglib prefix="ww" uri="/struts-tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
<ww:i18n name="org.codehaus.plexus.redback.xwork.default">
<head>
  <title><ww:text name="register.page.title"/></title>
</head>

<body>

<%@ include file="/WEB-INF/jsp/redback/include/formValidationResults.jsp" %>

<h2><ww:text name="register.section.title"/></h2>
   
<ww:form action="register" namespace="/security" theme="xhtml"
         id="registerForm" method="post" name="register" cssClass="security register">     
  
  <ww:textfield label="%{getText('username')}"         name="user.username" size="30" required="true"/>
  <ww:textfield label="%{getText('full.name')}"        name="user.fullName" size="30" required="true"/>
  <ww:textfield label="%{getText('email.address')}"    name="user.email" size="50"    required="true"/>

  <c:if test="${! emailValidationRequired}">
    <ww:password  label="%{getText('password')}"         name="user.password" size="20" required="true"/>
    <ww:password  label="%{getText('confirm.password')}" name="user.confirmPassword" size="20" required="true"/>
  </c:if>
  
  <ww:submit value="%{getText('register')}" method="register" />
  <ww:submit value="%{getText('cancel')}" method="cancel" />
</ww:form>

</body>
</ww:i18n>
</html>
