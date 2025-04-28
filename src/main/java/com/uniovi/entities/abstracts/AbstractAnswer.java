package com.uniovi.entities.abstracts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uniovi.interfaces.JsonEntity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
public abstract class AbstractAnswer implements JsonEntity {

	@Id
	@GeneratedValue
	private Long id;

	@JsonIgnore
	private String text;

	@JsonIgnore
	private boolean correct;

	protected AbstractAnswer(String text, boolean correct) {
		this.text    = text;
		this.correct = correct;
	}

	@Override
	public String toString() {
		return text;
	}
}