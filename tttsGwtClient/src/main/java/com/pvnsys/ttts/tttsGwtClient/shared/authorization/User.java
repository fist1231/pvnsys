package com.pvnsys.ttts.tttsGwtClient.shared.authorization;

import java.io.Serializable;

public class User implements Serializable {
    private Integer id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;

    public User(Integer id, String firstName, String lastName, String username,
			String password) {
		super();
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.password = password;
	}

	/**
     * Gets the email address for this user. When authenticating against this data directly, this is also used as the
     * username.
     *
     * @return
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the first name of the user.
     *
     * @return
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Gets the id for this user. When creating a new user this should be null, otherwise it will be non-null.
     *
     * @return
     */
    public Integer getId() {
        return id;
    }

    /**
     * Gets the last name of the user.
     *
     * @return
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Gets the password for this user. In some instances, this password is not actually used. For example, when an in
     * memory authentication is used the password on the spring security User object is used.
     *
     * @return
     */
    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // --- convenience methods ---

    /**
     * Gets the full name in a formatted fashion. Note in a real application a formatter may be more appropriate, but in
     * this application simplicity is more important.
     *
     * @return
     */
    public String getName() {
        return getLastName() + ", " + getFirstName();
    }

    // --- override Object ---

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    private static final long serialVersionUID = 8433999509932007961L;
}