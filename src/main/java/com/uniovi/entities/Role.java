package com.uniovi.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.common.aliasing.qual.Unique;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Role {
	@Id
	@Unique
	private String name;

	@ManyToMany(mappedBy = "roles", fetch = FetchType.EAGER)
	private Set<Player> players = new HashSet<>();

	public Role(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		Role role = (Role) o;
		return Objects.equals(name, role.name);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}
}
