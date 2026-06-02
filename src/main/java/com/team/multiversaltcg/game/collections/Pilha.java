package com.team.multiversaltcg.game.collections;

public class Pilha<T> {

    private Node<T> topo;
    private int tamanho;

    private static class Node<T> {
        T valor;
        Node<T> proximo;

        Node(T valor) {
            this.valor = valor;
        }
    }

    public void push(T item) {
        Node<T> novoNo = new Node<>(item);
        novoNo.proximo = topo;
        topo = novoNo;
        tamanho++;
    }

    public T pop() {
        if (estaVazia()) throw new RuntimeException("Pilha vazia");
        T valor = topo.valor;
        topo = topo.proximo;
        tamanho--;
        return valor;
    }

    public T peek() {
        if (estaVazia()) throw new RuntimeException("Pilha vazia");
        return topo.valor;
    }

    public boolean estaVazia() {
        return topo == null;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void clear() {
        topo = null;
        tamanho = 0;
    }

    public boolean contem(T item) {
        Node<T> atual = topo;
        while (atual != null) {
            if (atual.valor.equals(item)) return true;
            atual = atual.proximo;
        }
        return false;
    }

    public java.util.List<T> toList() {
        java.util.List<T> lista = new java.util.ArrayList<>();
        Node<T> atual = topo;
        while (atual != null) {
            lista.add(atual.valor);
            atual = atual.proximo;
        }
        return lista;
    }
}