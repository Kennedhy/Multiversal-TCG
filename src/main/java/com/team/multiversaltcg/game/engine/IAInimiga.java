package com.team.multiversaltcg.game.engine;

import com.team.multiversaltcg.game.enums.ModoAcao;
import com.team.multiversaltcg.game.model.AcaoTurno;
import com.team.multiversaltcg.game.model.Ataque;
import com.team.multiversaltcg.game.model.CampoBatalha;
import com.team.multiversaltcg.game.model.MonstroInstancia;

import java.util.ArrayList;
import java.util.List;

public class IAInimiga {

    public List<AcaoTurno> decidirAcoes(CampoBatalha campo) {
        List<AcaoTurno> acoes = new ArrayList<>();
        MonstroInstancia[] slots = campo.getSlotsInimigo();
        int auraDisponivel = campo.getAuraInimigo();

        for (int i = 0; i < slots.length; i++) {
            MonstroInstancia monstro = slots[i];
            if (monstro == null) continue;

            if (monstro.estaPressoDef()) {
                acoes.add(AcaoTurno.builder()
                        .slotOrigem(i)
                        .modo(ModoAcao.DEFESA)
                        .build());
                continue;
            }

            ModoAcao modo = escolherModo(monstro, campo, auraDisponivel);
            if (modo == ModoAcao.ATAQUE) {
                int indiceAtaque = escolherAtaque(monstro, auraDisponivel);
                Ataque ataque = monstro.getTemplate().getAtaque(indiceAtaque);
                auraDisponivel -= ataque.getCustoAura();

                int slotAlvo = escolherAlvo(campo.getSlotsJogador());
                boolean diretoLider = slotAlvo == -1;

                acoes.add(AcaoTurno.builder()
                        .slotOrigem(i)
                        .modo(ModoAcao.ATAQUE)
                        .indiceAtaque(indiceAtaque)
                        .slotAlvo(diretoLider ? 0 : slotAlvo)
                        .alvoDiretoLider(diretoLider)
                        .build());
            } else {
                acoes.add(AcaoTurno.builder()
                        .slotOrigem(i)
                        .modo(modo)
                        .build());
            }
        }

        return acoes;
    }

    private ModoAcao escolherModo(MonstroInstancia monstro, CampoBatalha campo,
                                  int auraDisponivel) {
        if (monstro.getPressure() >= 2) return ModoAcao.DEFESA;

        Ataque ataqueBasico = monstro.getTemplate().getAtaque(0);
        if (auraDisponivel < ataqueBasico.getCustoAura()) return ModoAcao.FARM;
        if (campo.getAuraInimigo() < 4 && monstro.getPressure() == 0) return ModoAcao.FARM;

        return ModoAcao.ATAQUE;
    }

    private int escolherAtaque(MonstroInstancia monstro, int auraDisponivel) {
        List<Ataque> ataques = monstro.getTemplate().getAtaques();
        int melhor = 0;

        for (int i = ataques.size() - 1; i >= 0; i--) {
            if (ataques.get(i).getCustoAura() <= auraDisponivel) {
                melhor = i;
                break;
            }
        }

        return melhor;
    }

    private int escolherAlvo(MonstroInstancia[] slots) {
        int melhorSlot = -1;
        int maiorPressao = -1;

        for (int i = 0; i < slots.length; i++) {
            if (slots[i] != null && slots[i].getPressure() > maiorPressao) {
                maiorPressao = slots[i].getPressure();
                melhorSlot = i;
            }
        }

        return melhorSlot;
    }
}
