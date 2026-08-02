package br.com.serasa.serasa.scale;

import br.com.serasa.serasa.branch.Branch;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scale")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scale {

    @Id
    @Column(length = 30)
    private String code;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(length = 120)
    private String location;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Version
    private Long version;
}
