package org.springframework.samples.petclinic;

/**
 * Simple JavaBean domain object representing an person.
 *
 * @author Ken Krebs
 * @author Arne Limburg
 */
public class Person extends BaseEntity {

	private String firstName;

	private String lastName;

	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String toString() {
	    return this.getLastName() + ", " + this.getFirstName();
    }
}
