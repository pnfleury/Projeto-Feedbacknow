package br.com.feedbacknow.api_feedbacknow.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; //admin, user

    public Role() {}

    public Role(String name){
        this.name = name;
    }
}
