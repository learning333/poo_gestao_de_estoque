package br.com.ufabc.poogestaodeestoque;

import java.util.Optional;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.ufabc.poogestaodeestoque.controle.CrudLoteCompraService;
import br.com.ufabc.poogestaodeestoque.controle.CrudProdutoService;
import br.com.ufabc.poogestaodeestoque.controle.CrudVendaService;
import br.com.ufabc.poogestaodeestoque.modelo.LoteCompra;
import br.com.ufabc.poogestaodeestoque.modelo.Venda;

@Service
public class VisaoVendas {
	
	@Autowired
	private CrudProdutoService crudProduto;
	@Autowired
	private CrudLoteCompraService crudLote;
	@Autowired
	private CrudVendaService crudVenda;
	

	
	
	public void menu(Scanner scanner) {
		boolean gatilho=true;
		while(gatilho) {
			System.out.println("\nEscolha acao");
			System.out.println("0-voltar");
			System.out.println("1-Nova Venda");
			System.out.println("2-Listar Vendas");
			System.out.println("3-Listar Produtos Em Maos");
			System.out.println("4-Devolucao");

			
			int opcao=scanner.nextInt();
			
			switch(opcao) {
			case 1:
				this.cadastrar(scanner);
				break;
			case 2:
				this.visualizar();
				break;
			case 3:
				this.visualizarEmMaos();
				break;
			case 4:
				this.devolucao(scanner);
				break;
			default:
				gatilho=false;
			}
			
		}
		System.out.println();
	}
	private void listarLotesEmMaos() {
		System.out.println("--------------Listando Lotes Em Maos--------------");
		Iterable<LoteCompra> lista = this.crudLote.listarLotes();
		for(LoteCompra lote: lista) {
			if(lote.getStatus().equals("em maos")){
				System.out.println(lote.listagemParaVenda());
			}
		}
		System.out.println("-----------------------Fim------------------------");
	}
	private void cadastrar(Scanner scanner) {
		listarLotesEmMaos();
		System.out.print("Digite o Id do lote de origem do produto a ser vendido: ");
		Long idlote=scanner.nextLong();
		
		Optional<LoteCompra> optional = this.crudLote.buscaPeloId(idlote);
		if(optional.isPresent()) {
			LoteCompra lote = optional.get();
			int qtdDisponivel=lote.getQtd()-lote.getQtdVendida();
			if(lote.getStatus().equals("em maos")) {//tem quantidade disponivel>0
				
				System.out.println("Produto:["+lote.getNomeProduto()+"]");
				System.out.println("Quantidade Disponivel:["+qtdDisponivel+"]");

				System.out.print("Qtd vendida:");
				int qtd=scanner.nextInt();
				
				
				boolean filtro=true;
				while(filtro) {
					if(qtd>qtdDisponivel) {
						System.out.print("Quantidade inserida ["+qtd+"] maior do que quantidade disponivel ["+qtdDisponivel+"]" );
						System.out.print( "\nDigite um valor valido: ");
						qtd=scanner.nextInt();
					}else {//digitou quantidade valida

						filtro=false;
					}
				}

				
				System.out.print("Preco unitario:");
				float precoVenda=scanner.nextFloat();
				float valorTotal=qtd*precoVenda;
				
				System.out.print("nome cliente: ");
				String nome=scanner.next();
				
				float lucro=(precoVenda-lote.getCusto())*qtd;
				
				Venda novaVenda=this.crudVenda.adicionarNovo(nome,precoVenda,lote,qtd,lucro);
				
							
				//lote.setQtdVendida(qtd);//qtdvendida+=qtd
				if(lote.getQtd()==lote.getQtdVendida()) {
					this.crudLote.encerraLote(lote);//vendeu toda quantidade do lote de compra
				}
				System.out.println(novaVenda);
				
				System.out.print("Salvo\n");
			}else {
				System.out.print("Pedido de compra nao disponivel [status="+lote.getStatus()+"]");
			}
		}else {
			System.out.print("id nao existe");
		}

	}
	private void visualizar() {
		System.out.println("-----------------Listando Vendas------------------");

		Iterable<Venda> lista = this.crudVenda.listarVendas();
		for(Venda venda: lista) {
				System.out.println(venda);
				System.out.println("\n");
		}
		System.out.println("-----------------------Fim------------------------");
		System.out.println();
	}
	private void visualizarEmMaos() {

		System.out.println("--------------Listando Lotes Em Maos--------------");

		Iterable<LoteCompra> lista = this.crudLote.listarLotes();
		for(LoteCompra lote: lista) {
			if(lote.getStatus().equals("em maos")){
				System.out.println(lote);
				System.out.println("\n");
			}
		}
		System.out.println("-----------------------Fim------------------------");
		System.out.println();
		
	}

	private void devolucao(Scanner scanner) {
		
		visualizar();
		
		System.out.print("Digite o id da venda: ");
		Long id=scanner.nextLong();
		Optional<Venda> optional = this.crudVenda.buscaPeloId(id);
		if(optional.isPresent()) {
			Venda venda = optional.get();
			
			venda=this.crudVenda.entradaDevolucao(venda);
			
			//devolver quantidade para o lotecompra

			LoteCompra lotecompra=venda.getLote();//optLote.get();
			//lotecompra.setQtdVendida(-venda.getQtd());
			if(lotecompra.getStatus().equals("encerrado")) {//se estava zerado reativa o lote compra
				lotecompra=crudLote.reativaLote(lotecompra, -venda.getQtd());
				
			}
			System.out.println("Venda devolvida: ");
			System.out.println(venda);
			System.out.println("------------------------------");
			System.out.println("Item retornado ao estoque: ");
			System.out.println(lotecompra);
			System.out.println("------------------------------");
			System.out.print("Concluido!\n");
		}else {
			System.out.print("ID do lote nao existe");
		}
	}
}