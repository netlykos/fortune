package org.netlykos.fortune.beans;

import java.util.List;

/**
 * A record class that represents a fortune cookie.
 *
 * @author netlykos
 */
public record Fortune(String category, Integer number, List<String> lines) { }
