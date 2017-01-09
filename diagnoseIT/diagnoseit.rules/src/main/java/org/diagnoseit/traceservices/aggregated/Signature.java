package org.diagnoseit.traceservices.aggregated;

import java.util.List;

public class Signature {

	private String returnType;
	private String packageName;
	private String className;
	private String methodName;
	private List<String> parameterTypes;
	private String signature;

	public Signature() {
	}

	/**
	 * @param returnType
	 * @param packageName
	 * @param className
	 * @param methodName
	 * @param parameterTypes
	 */
	public Signature(String returnType, String packageName, String className, String methodName, List<String> parameterTypes) {
		this.returnType = returnType;
		this.packageName = packageName;
		this.className = className;
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
	}
	
	public Signature(String signature) {
		this.signature = signature;
	}

	/**
	 * @return the returnType
	 */
	public String getReturnType() {
		return returnType;
	}

	/**
	 * @param returnType
	 *            the returnType to set
	 */
	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	/**
	 * @return the packageName
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @param packageName
	 *            the packageName to set
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * @return the className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 *            the className to set
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @param methodName
	 *            the methodName to set
	 */
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * @return the parameterTypes
	 */
	public List<String> getParameterTypes() {
		return parameterTypes;
	}

	/**
	 * @param parameterTypes
	 *            the parameterTypes to set
	 */
	public void setParameterTypes(List<String> parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public String getSignatureString() {
		return this.signature;
	}
	
	private String buildSignature() {
		StringBuilder strBuilder = new StringBuilder();
		boolean first = true;
		for (String pType : getParameterTypes()) {
			if (first) {
				first = false;
			} else {
				strBuilder.append(",");
			}
			strBuilder.append(pType);

		}
		return getPackageName() + "." + getClassName() + "." + getMethodName() + "(" + strBuilder.toString() + ")";

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
		result = prime * result + ((parameterTypes == null) ? 0 : parameterTypes.hashCode());
		result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Signature other = (Signature) obj;

		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		if (parameterTypes == null) {
			if (other.parameterTypes != null)
				return false;
		} else if (!parameterTypes.equals(other.parameterTypes))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		return true;
	}

}
