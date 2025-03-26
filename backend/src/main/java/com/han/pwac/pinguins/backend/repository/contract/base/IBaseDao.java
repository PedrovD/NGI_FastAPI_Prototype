package com.han.pwac.pinguins.backend.repository.contract.base;

import java.util.Collection;
import java.util.Optional;

public interface IBaseDao<T, TId> {
    /**
     * gets all the {@link T} objects
     *
     * @return all the {@link T} objects
     */
    Collection<T> getAll();

    /**
     * finds the {@link T} value using the given id
     *
     * @param id the id to search for
     * @return an optional value with the {@link T} object if the id was found else a null option
     */
    Optional<T> findById(TId id);

    /**
     * adds an object of type {@link T} to the database
     *
     * @param item the object to add
     * @return true if the object was added else false
     */
    boolean add(T item);

    /**
     * deletes an object from the database with the given id
     *
     * @param id the id of the object to delete
     * @return true if the item was deleted else false
     */
    boolean delete(TId id);
    /**
     * updates an object in the database with the given id
     *
     * @param id   the id of the object to update
     * @param item the new object to override it with
     * @return true the object was updated else false
     */
    boolean update(TId id, T item);
    /**
     * gets the last inserted id
     *
     * @return the last inserted id
     */
    TId getLastInsertedId();
}
