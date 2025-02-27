package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=726344e1";
    private final String NUMERO_DA_TEMPORADA = "&season=";
    private final String NUMERO_DO_EP = "&episode=" ;
    private final List<DadosSerie> dadosSeries = new ArrayList<>();
    private SerieRepository repositorio;
    private List<Serie> series  = new ArrayList<>();
    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {

        var opcao = -1;
        while (opcao != 0){
            var menu = """
                1 - Buscar séries
                2 - Buscar Todas os Episodios de Uma Serie - Contém Lista!
                3 - Exibir Lista de Series Buscadas
                4 - Exibir Informações Sobre Episodio
                5 - Buscar Serie
                
                0 - Sair                                 
                """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodiosPorSerie();
                    break;
                case 3:
                    exibirSeriesBuscadas();
                    break;
                case 4:
                    exibirInfosSobreEpisodio();
                    break;
                case 5:
                    buscarSeriePorTitulo();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }

    }



    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
//        dadosSeries.add(dados);
        repositorio.save(serie);
//        List<Serie> exibirSerieTraduzida;
//        exibirSerieTraduzida = dadosSeries.stream()
//                .map(d -> new Serie(d))
//                .collect(Collectors.toList());
//        exibirSerieTraduzida.stream()
//                .sorted(Comparator.comparing(Serie::getSinopse))
//                .forEach(System.out::println); (Antes eu fazia este passo para exibir o dados que um DadosSerie no modelo da Serie. Basicamente eu criava uma List baseada em Serie, depois fazia uma stream do objeto dadosSerie, que é um objeto da classeRecord DadosSerie e fazia um mapemaneto para cada objeto que cada objeto desta lista serie um novo objeto de Serie e coletava esses objetos em uma lista, que seria a Lista que eu criei de Serie. Depois percoria a lista e fazendo o comparing printando elas por sinopse.))
        System.out.println(serie);

    }



    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodiosPorSerie(){
        exibirSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nomeSerie.toLowerCase()))
                .findFirst();

        if (serie.isPresent()){
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);

            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        }else {
            System.out.println("Série não encontrada!");
        }


    }


    private void exibirSeriesBuscadas(){
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getId))
                .forEach(System.out::println);
    }

    private void exibirInfosSobreEpisodio(){
        System.out.println("Digite o nome da Serie");
        var nomeSerie = leitura.nextLine();
        System.out.println("Digite a temporada");
        var numeroTemporada = leitura.nextLine();
        System.out.println("Digite o episodio");
        var numeroEpisodio = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + NUMERO_DA_TEMPORADA + numeroTemporada + NUMERO_DO_EP + numeroEpisodio + API_KEY);
        DadosAtores dados = conversor.obterDados(json, DadosAtores.class);
        System.out.println(dados);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serieBuscada = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBuscada.isPresent()){
            System.out.println("Dados da Série" + serieBuscada.get());
        } else {
            System.out.println("Serie não encontrada!");
        }
    }


}