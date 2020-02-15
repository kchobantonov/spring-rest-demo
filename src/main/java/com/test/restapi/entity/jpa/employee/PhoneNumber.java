package com.test.restapi.entity.jpa.employee;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "PHONE")
@IdClass(PhoneNumber.ID.class)
public class PhoneNumber implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "EMP_ID", updatable = false, insertable = false)
	private Integer id;

	@Id
	@Column(updatable = false)
	private String type;

	@Basic
	@Column(name = "AREA_CODE")
	private String areaCode;

	@Basic
	@Column(name = "P_NUMBER")
	private String number;

	@ManyToOne
	@JoinColumn(name = "EMP_ID")
	private Employee owner;

	public PhoneNumber() {
	}

	public PhoneNumber(String type, String areaCode, String number) {
		this();
		setType(type);
		setAreaCode(areaCode);
		setNumber(number);
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer empId) {
		this.id = empId;
	}

	public String getNumber() {
		return this.number;
	}

	public void setNumber(String pNumber) {
		this.number = pNumber;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Employee getOwner() {
		return this.owner;
	}

	protected void setOwner(Employee employee) {
		this.owner = employee;
		if (employee != null) {
			this.id = employee.getId();
		}
	}

	public static class ID implements Serializable {
		private static final long serialVersionUID = 1L;

		public Integer id;
		public String type;

		public ID() {
		}

		public ID(Integer empId, String type) {
			this.id = empId;
			this.type = type;
		}

		public boolean equals(Object other) {
			if (other instanceof ID) {
				final ID otherID = (ID) other;
				return otherID.id.equals(id) && otherID.type.equals(type);
			}
			return false;
		}

		public int hashCode() {
			return super.hashCode();
		}
	}
}