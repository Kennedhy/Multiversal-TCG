package com.team.multiversaltcg.game.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListaDuplamenteEncadeada<T> {

    private Node<T> head;
    private Node<T> tail;
    private int tamanho;

    private static class Node<T> {
        T valor;
        Node<T> proximo;
        Node<T> anterior;

        Node(T valor) {
            this.valor = valor;
        }
    }

    public void addFirst(T item) {
        Node<T> novoNo = new Node<>(item);
        if (estaVazia()) {
            head = novoNo;
            tail = novoNo;
        } else {
            novoNo.proximo = head;
            head.anterior = novoNo;
            head = novoNo;
        }
        tamanho++;
    }

    public void addLast(T item) {
        Node<T> novoNo = new Node<>(item);
        if (estaVazia()) {
            head = novoNo;
            tail = novoNo;
        } else {
            novoNo.anterior = tail;
            tail.proximo = novoNo;
            tail = novoNo;
        }
        tamanho++;
    }

    public T removeFirst() {
        if (estaVazia()) throw new RuntimeException("Lista vazia");
        T valor = head.valor;
        head = head.proximo;
        if (head != null) {
            head.anterior = null;
        } else {
            tail = null;
        }
        tamanho--;
        return valor;
    }

    public T removeLast() {
        if (estaVazia()) throw new RuntimeException("Lista vazia");
        T valor = tail.valor;
        tail = tail.anterior;
        if (tail != null) {
            tail.proximo = null;
        } else {
            head = null;
        }
        tamanho--;
        return valor;
    }

    public T get(int indice) {
        if (indice < 0 || indice >= tamanho) {
            throw new IndexOutOfBoundsException("Indice invalido: " + indice);
        }
        Node<T> atual = head;
        for (int i = 0; i < indice; i++) {
            atual = atual.proximo;
        }
        return atual.valor;
    }

    public void shuffle() {
        List<T> lista = toList();
        Collections.shuffle(lista);
        clear();
        for (T item : lista) {
            addLast(item);
        }
    }

    public boolean estaVazia() {
        return head == null;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void clear() {
        head = null;
        tail = null;
        tamanho = 0;
    }

    public List<T> toList() {
        List<T> lista = new ArrayList<>();
        Node<T> atual = head;
        while (atual != null) {
            lista.add(atual.valor);
            atual = atual.proximo;
        }
        return lista;
    }

    public List<T> peekTop(int n) {
        List<T> lista = new ArrayList<>();
        Node<T> atual = head;
        int count = 0;
        while (atual != null && count < n) {
            lista.add(atual.valor);
            atual = atual.proximo;
            count++;
        }
        return lista;
    }
}
