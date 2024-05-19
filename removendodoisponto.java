package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class removendodoisponto {
    public static void main(String[] args) {
        String nomeArquivoEntrada = "C:\\Temp\\mesorigiao.CSV";
        String nomeArquivoSaida = "C:\\Temp\\fk\\mesorigiao.CSV";

        lerECriarCSV(nomeArquivoEntrada, nomeArquivoSaida);
    }

    public static void lerECriarCSV(String nomeArquivoEntrada, String nomeArquivoSaida) {
        try {
            BufferedReader leitor = new BufferedReader(new FileReader(nomeArquivoEntrada, StandardCharsets.ISO_8859_1));
            BufferedWriter escritorCSV = new BufferedWriter(new FileWriter(nomeArquivoSaida, StandardCharsets.ISO_8859_1));

            // Armazenar e escrever a primeira linha
            String primeiraLinha = leitor.readLine();
            escritorCSV.write(primeiraLinha);
            escritorCSV.newLine();

            String linha;
            while ((linha = leitor.readLine()) != null) {
                // Dividir a linha usando ":" como separador
                String[] dados = linha.split(":");
                // Escrever os dados no arquivo CSV
                for (String dado : dados) {
                    escritorCSV.write(dado + ";");
                }
                escritorCSV.newLine();
            }

            leitor.close();
            escritorCSV.flush();
            escritorCSV.close();

            System.out.println("Arquivo CSV criado com sucesso.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
