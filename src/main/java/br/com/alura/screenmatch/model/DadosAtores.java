package br.com.alura.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DadosAtores(
                          @JsonAlias("Writer") String escritores,
                          @JsonAlias("Actors") String atores,
                          @JsonAlias("Plot") String sinopse,
                          @JsonAlias("Poster") String posterDoEp) {

    @Override
    public String toString() {
        return "Sinopse: " + sinopse + "\nAtores: " + atores + "\nPoster: " + posterDoEp  + "\nEscritores: " + escritores;
    }
}
