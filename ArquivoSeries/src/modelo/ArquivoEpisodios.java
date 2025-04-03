package modelo;

import aeds3.*;
import entidades.Episodio;
import java.util.ArrayList;

public class ArquivoEpisodios extends Arquivo<Episodio> {

    private ArvoreBMais<ParNameEpisodioID> indiceNome;

    public ArquivoEpisodios() throws Exception {
        super("episodios", Episodio.class.getConstructor());
        indiceNome = new ArvoreBMais<>(
            ParNameEpisodioID.class.getConstructor(), 
            5, 
            "./dados/" + nomeEntidade + "/indiceNome.db"
        );
    }

    @Override
    public int create(Episodio e) throws Exception {
        int id = super.create(e);
        indiceNome.create(new ParNameEpisodioID(e.getNome(), id));
        return id;
    }

    public Episodio[] readNome(String nome) throws Exception {
        if (nome == null || nome.isEmpty())
            return null;
        ArrayList<ParNameEpisodioID> ptis = indiceNome.read(new ParNameEpisodioID(nome, -1));
        if (!ptis.isEmpty()) {
            Episodio[] episodios = new Episodio[ptis.size()];
            for (int i = 0; i < ptis.size(); i++) {
                episodios[i] = read(ptis.get(i).getId());
            }
            return episodios;
        }
        return null;
    }

    @Override
    public boolean delete(int id) throws Exception {
        Episodio e = read(id);
        if (e != null) {
            if (super.delete(id)) {
                return indiceNome.delete(new ParNameEpisodioID(e.getNome(), id));
            }
        }
        return false;
    }

    @Override
    public boolean update(Episodio novoEpisodio) throws Exception {
        Episodio e = read(novoEpisodio.getID());
        if (e != null) {
            String nomeAntigo = e.getNome();
            String nomeNovo = novoEpisodio.getNome();
            int id = novoEpisodio.getID();
            if (super.update(novoEpisodio)) {
                if (!nomeAntigo.equals(nomeNovo)) {
                    indiceNome.delete(new ParNameEpisodioID(nomeAntigo, id));
                    indiceNome.create(new ParNameEpisodioID(nomeNovo, id));
                }
                return true;
            }
        }
        return false;
    }

    public void close() throws Exception {
        if (indiceNome != null) {
            indiceNome.close();
        }
    }
}
