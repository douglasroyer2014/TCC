package com.br.manager.caged;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CagedDirectory {

    String directory;
    String tableName;
    boolean isStructured;
}
