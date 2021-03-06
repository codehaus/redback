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

<c:choose>
  <c:when test="${user.edit}">
    <ww:label     label="Username"         name="user.username" />
    <ww:hidden    name="user.username" />
  </c:when>
  <c:otherwise>
    <ww:textfield label="Username"         name="user.username" size="30" required="true"/>
  </c:otherwise>
</c:choose>

  <ww:textfield label="Full Name"        name="user.fullName" size="30" required="true"/>
  <ww:textfield label="Email Address"    name="user.email" size="50"    required="true"/>
  <c:choose>
    <c:when test="${self}">
      <ww:password  label="Current Password"         name="oldPassword" size="20" required="true"/>
      <ww:password  label="New Password"         name="user.password" size="20" required="true"/>
    </c:when>
    <c:otherwise>
      <ww:password  label="Password"         name="user.password" size="20" required="true"/>
    </c:otherwise>
  </c:choose>
  <ww:password  label="Confirm Password" name="user.confirmPassword" size="20" required="true"/>

<ww:if test="%{user.timestampAccountCreation != null}">
  <ww:label     label="Account Creation"     name="user.timestampAccountCreation" />
</ww:if>

<ww:if test="%{user.timestampLastLogin != null}">
  <ww:label     label="Last Login"           name="user.timestampLastLogin" />
</ww:if>

<ww:if test="%{user.timestampLastPasswordChange != null}">
  <ww:label     label="Last Password Change" name="user.timestampLastPasswordChange" />
</ww:if>
