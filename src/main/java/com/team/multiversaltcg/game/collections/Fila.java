package com.team.multiversaltcg.game.collections;

public class Fila<T> {

    private Node<T> frente;
    private Node<T> fim;
    private int tamanho;

    private static class Node<T> {
        T valor;
        Node<T> proximo;

        Node(T valor) {
            this.valor = valor;
        }
    }

    public void enqueue(T item) {
        Node<T> novoNo = new Node<>(item);
        if (estaVazia()) {
            frente = novoNo;
        } else {
            fim.proximo = novoNo;
        }
        fim = novoNo;
        tamanho++;
    }

    public T dequeue() {
        if (estaVazia()) throw new RuntimeException("Fila vazia");
        T valor = frente.valor;
        frente = frente.proximo;
        if (frente == null) fim = null;
        tamanho--;
        return valor;
    }

    public T peek() {
        if (estaVazia()) throw new RuntimeException("Fila vazia");
        return frente.valor;
    }

    public boolean estaVazia() {
        return frente == null;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void clear() {
        frente = null;
        fim = null;
        tamanho = 0;
    }

    public java.util.List<T> toList() {
        java.util.List<T> lista = new java.util.ArrayList<>();
        Node<T> atual = frente;
        while (atual != null) {
            lista.add(atual.valor);
            atual = atual.proximo;
        }
        return lista;
    }
}