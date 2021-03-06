<%--
  ~ Copyright 2005-2006 The Codehaus.
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
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<html>
<ww:i18n name="org.codehaus.plexus.redback.xwork.default">
<head>
  <title><ww:text name="login.page.title"/></title>
</head>

<body onload="javascript:document.forms['login'].username.focus();">


<c:choose>
  <c:when test="${sessionScope.securitySession.authenticated != true}">
  
  <h2><ww:text name="login.section.title"/></h2>

  <%@ include file="/WEB-INF/jsp/redback/include/formValidationResults.jsp" %>
  
  
    <ww:form action="login" namespace="/security" theme="xhtml" 
         id="loginForm" method="post" name="login" cssClass="security login">
      <ww:textfield label="%{getText('username')}" name="username" size="30" />
      <ww:password  label="%{getText('password')}" name="password" size="20" />
      <ww:checkbox label="%{getText('login.remember.me')}" name="rememberMe" value="false" />
      <ww:submit value="%{getText('login')}" method="login" />
      <ww:submit value="%{getText('cancel')}" method="cancel" />
  </ww:form>
<%-- TODO: Figure out how to auto-focus to first field --%>

<ul class="tips">
  <%--
  <li>
     Forgot your Username? 
     <ww:url id="forgottenAccount" action="findAccount" />
     <ww:a href="%{forgottenAccount}">Email me my account information.</ww:a>
  </li>
    --%>
  <redback:isNotReadOnlyUserManager>
  <li>
     <ww:text name="login.need.an.account"/>
     <ww:url id="registerUrl" action="register" />
     <ww:a href="%{registerUrl}"><ww:text name="login.register"/></ww:a>
  </li>
  <li>
     <ww:text name="login.forgot.your.password"/>
     <ww:url id="forgottenPassword" action="passwordReset" />
     <ww:a href="%{forgottenPassword}"><ww:text name="login.request.password.reset"/></ww:a>
  </li>
  </redback:isNotReadOnlyUserManager>
</ul>
</c:when>
<c:otherwise>
  <p/>
	<ww:text name="login.already.logged.in"/>
  <p/>
</c:otherwise>
</c:choose>
</body>
</ww:i18n>
</html>
