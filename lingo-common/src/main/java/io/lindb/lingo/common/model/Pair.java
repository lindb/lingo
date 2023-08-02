package io.lindb.lingo.common.model;

import lombok.Data;

@Data
public class Pair<K, V> {
	private K key;
	private V value;

	public Pair() {
	}

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}
}
