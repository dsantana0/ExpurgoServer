package com.indra.main;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
	
	final static String diretorioPrincipal = "D:\\Default - Cópia (2)";
	static Integer contLogDeletados = 0;
	
public static void main(String[] args) throws IOException {
		
		Calendar horaExecucao = Calendar.getInstance();
		horaExecucao.set(Calendar.HOUR_OF_DAY, 9); 		// Hora da Execução
		horaExecucao.set(Calendar.MINUTE, 25); 			// Minuto da Execução
		horaExecucao.set(Calendar.SECOND, 00);			// Segundo da Execução
		Date hora = horaExecucao.getTime();
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				System.out.println("Inicializando Expurgo de LOGs agendado");
				//Diretório Raiz
				Path pastaDefault = Paths.get(diretorioPrincipal);
				if (Files.exists(pastaDefault, LinkOption.NOFOLLOW_LINKS)) {
					percorrerDiretorio(pastaDefault); // Método que percorre as Pastas dentro do diretório principal
					System.out.println("Expurgo finalizado, "+contLogDeletados+" arquivos deletados.");
				}else {
					System.out.println("Diretório principal não encontrado, expurgo cancelado.");
				} 			
				
			}
		},hora);
	}

	private static void percorrerDiretorio(Path path) {
		
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
			for (Path caminho : stream) {
				if (Files.isDirectory(caminho)) {
					percorrerDiretorio(caminho); // Recursividade para percorrer as pastas dentro de uma pasta
				}
				
				if (caminho.getFileName().toString().toLowerCase().endsWith(".pdf")) { 		//Verifica extensão do arquivo
					BasicFileAttributes atributosArquivo = Files.readAttributes(caminho, BasicFileAttributes.class);
					if (verificaDataCriacao(atributosArquivo.creationTime().toMillis())) {
						System.out.println("Log excluído: "+caminho.getParent()+"\\"+caminho.getFileName());
						if (Files.deleteIfExists(caminho)) { // Verifica se o arquivo realmente foi excluído
							contLogDeletados++;
						}
					}
				}
			}
		} catch (AccessDeniedException ade) {
			System.err.println("O arquivo não excluído por falta de permissão.");
		} catch (FileSystemException fse) {
			System.err.println("Arquivo está sendo utilizado por outro processo não excluído : ");
		} catch (IOException e) {
			System.err.print("Erro: ");
			e.printStackTrace();
		}
	}

	private static boolean verificaDataCriacao(long dataCriacao) {
		
		//Método Verifica se a data de Criação do Arquivo é igual ou superior a 15 Dias
		Date dataArquivo = new Date(dataCriacao);
		Date dataAtual = new Date();
		long diferencaDias = (dataAtual.getTime() - dataArquivo.getTime()) / (1000 * 60 * 60 * 24);
		if (diferencaDias > 1) {
			return true;
		} else {
			return false;
		}
	}

}
