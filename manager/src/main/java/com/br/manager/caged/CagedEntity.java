package com.br.manager.caged;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CagedEntity {

    String tableName;
    String defaultSearch;
    int defaultValue;
    String fieldSearch;
    String valueSearch;
}