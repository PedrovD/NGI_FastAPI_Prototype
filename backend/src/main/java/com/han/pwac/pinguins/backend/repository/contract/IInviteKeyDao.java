package com.han.pwac.pinguins.backend.repository.contract;

import com.han.pwac.pinguins.backend.domain.InviteKey;

import java.util.Optional;

public interface IInviteKeyDao extends IBaseDao<InviteKey>{
    Optional<InviteKey> findByKey(String id);
}
