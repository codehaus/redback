<%--
  ~ Copyright 2006 The Apache Software Foundation.
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
<%@ taglib prefix="ww" uri="/webwork" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://plexus.codehaus.org/redback/taglib-1.0" prefix="redback" %>

<c:choose>
  <c:when test="${user.edit}">
    <ww:label label="%{getText('username')}"         name="user.username" />
    <ww:hidden name="user.username" />
  </c:when>
  <c:otherwise> 
  	<redback:isReadOnlyUserManager>
	  <ww:label label="%{getText('username')}"         name="user.username" />
	</redback:isReadOnlyUserManager>
	<redback:isNotReadOnlyUserManager>
      <ww:textfield label="%{getText('username')}"         name="user.username" size="30" required="true"/>
	</redback:isNotReadOnlyUserManager>
  </c:otherwise>
</c:choose>

 
  
  <redback:isReadOnlyUserManager>
    <ww:label label="%{getText('full.name')}"         name="user.fullName" />
  </redback:isReadOnlyUserManager>
  <redback:isNotReadOnlyUserManager>
    <ww:textfield label="%{getText('full.name')}"        name="user.fullName" size="30" required="true"/>
  </redback:isNotReadOnlyUserManager>
  
  <redback:isReadOnlyUserManager>
    <ww:label label="%{getText('email.address')}"         name="user.email" />
  </redback:isReadOnlyUserManager>
  <redback:isNotReadOnlyUserManager>
    <ww:textfield label="%{getText('email.address')}"    name="user.email" size="50"    required="true"/>
  </redback:isNotReadOnlyUserManager>
  
  <redback:isNotReadOnlyUserManager>
  <c:choose>
    <c:when test="${self}">
      <ww:password  label="%{getText('current.password')}"         name="oldPassword" size="20" required="true"/>
      <ww:password  label="%{getText('new.password')}"         name="user.password" size="20" required="true"/>
    </c:when>
    <c:otherwise>
      <ww:password  label="%{getText('password')}"         name="user.password" size="20" required="true"/>
    </c:otherwise>
  </c:choose>
  <ww:password  label="%{getText('confirm.password')}" name="user.confirmPassword" size="20" required="true"/>
  </redback:isNotReadOnlyUserManager>

<ww:if test="%{user.timestampAccountCreation != null}">
  <ww:label     label="%{getText('account.creation')}"     name="user.timestampAccountCreation" />
</ww:if>

<ww:if test="%{user.timestampLastLogin != null}">
  <ww:label     label="%{getText('last.login')}"           name="user.timestampLastLogin" />
</ww:if>

<ww:if test="%{user.timestampLastPasswordChange != null}">
  <ww:label     label="%{getText('last.password.change')}" name="user.timestampLastPasswordChange" />
</ww:if>
