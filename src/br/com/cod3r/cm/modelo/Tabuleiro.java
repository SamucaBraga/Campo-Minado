package br.com.cod3r.cm.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Tabuleiro implements CampoObservador {

	private int qtdeLinhas;
	private int qtdeColunas;
	private int qtdeMinas;
	
	private final List<Campo> campos = new ArrayList<>();
	private final List<Consumer<ResultadoEvento>> observadores =
			new ArrayList<>();

	public Tabuleiro(int qtdeLinhas, int qtdeColunas, int qtdeMinas) {
		this.qtdeLinhas = qtdeLinhas;
		this.qtdeColunas = qtdeColunas;
		this.qtdeMinas = qtdeMinas;
		
		gerarCampos();
		associarVizinhos();
		sortearMinas();
	}
	
	public void registrarObservador(Consumer<ResultadoEvento> observador) {
		observadores.add(observador);
	}
	
	private void notificarObservadores(boolean resultado) {
		observadores.stream()
			.forEach(obs -> obs.accept(new ResultadoEvento(resultado))); 
	}
	
	public void abrir(int linha, int coluna) {
		campos.parallelStream()
			.filter(c -> c.getLinha() == linha && c.getColuna() == coluna)
			.findFirst()
			.ifPresent(c -> c.abrir());
	}
	
	public void alternarMarcacao(int linha, int coluna) {
		campos.parallelStream()
			.filter(c -> c.getLinha() == linha && c.getColuna() == coluna)
			.findFirst()
			.ifPresent(c -> c.alternarMarcacao());
	}

	private void gerarCampos() {
		for (int l = 0; l < qtdeLinhas; l++) {
			for (int c = 0; c < qtdeColunas; c++) {
				Campo campo = new Campo(l, c);
				campo.registrarObservador(this);
				campos.add(new Campo(l, c));
			}
		}
	}	
	
	private void associarVizinhos() {
		for (Campo c1 : campos) {
			for (Campo c2 : campos) {
				c1.adicionarVizinho(c2);
			}
		}
	}
	
	private void sortearMinas() {
		long minasArmadas = 0;
		Predicate<Campo> minado = c -> c.isMinado();
		 
		do {
			int aleatorio = (int) (Math.random() * campos.size());
			campos.get(aleatorio).minar();
			minasArmadas = campos.stream().filter(minado).count();
		} while(minasArmadas < qtdeMinas);
	}
	
	public boolean objetivoAlcancado() {
		return campos.stream().allMatch(c -> c.objetivoAlcançado());
	}
	
	public void reiniciar() {
		campos.stream().forEach(c -> c.reiniciar());
		sortearMinas();
	}
	
	@Override
	public void eventoOcorreu(Campo campo, CampoEvento evento) {
		if(evento == CampoEvento.EXPLODIR) {
			mostrarMinas();
			notificarObservadores(false);
		} else if(objetivoAlcancado()) {
			notificarObservadores(true);
		}
	}
	
	private void mostrarMinas() {
		campos.stream()
			.filter(c -> c.isMinado())
			.forEach(c -> c.setAberto(true));
	}
	
}
