package com.rdvmindset.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "industries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Industry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(mappedBy = "industries")
    private List<Company> companies;
}
