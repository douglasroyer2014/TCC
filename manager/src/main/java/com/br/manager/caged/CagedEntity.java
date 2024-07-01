package com.br.manager.caged;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CagedEntity {

    String tableName;
    String defaultSearch;
    List<String> valueDefaultSearch;
    String fieldSearch;
    String valueSearch;
    boolean structured;
}
