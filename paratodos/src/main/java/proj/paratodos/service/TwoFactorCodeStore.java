package proj.paratodos.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public class TwoFactorCodeStore {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void invalidateUnusedCodes(Long userId) {
        em.createNativeQuery("""
                UPDATE usuarios_2fa_codes
                SET usado = true
                WHERE usuario_id = :uid
                  AND usado = false
                """)
          .setParameter("uid", userId)
          .executeUpdate();
    }

    @Transactional
    public void insertCode(Long userId, String codeHash, LocalDateTime expiraEm) {
        em.createNativeQuery("""
                INSERT INTO usuarios_2fa_codes (usuario_id, codigo, codigo_hash, expira_em, usado, criado_em)
                VALUES (:uid, :hash, :hash, :exp, false, now())
                """)
        .setParameter("uid", userId)
        .setParameter("hash", codeHash)
        .setParameter("exp", expiraEm)
        .executeUpdate();
    }

    public String findLatestValidHash(Long userId) {
        var rows = em.createNativeQuery("""
                SELECT codigo_hash
                FROM usuarios_2fa_codes
                WHERE usuario_id = :uid
                AND usado = false
                AND expira_em > now()
                ORDER BY criado_em DESC
                LIMIT 1
                """)
        .setParameter("uid", userId)
        .getResultList();

        if (rows.isEmpty()) return null;
        return rows.get(0).toString();
    }

    @Transactional
    public void markUsed(Long userId, String codeHash) {
        em.createNativeQuery("""
                UPDATE usuarios_2fa_codes
                SET usado = true
                WHERE usuario_id = :uid
                  AND codigo_hash = :hash
                  AND usado = false
                """)
          .setParameter("uid", userId)
          .setParameter("hash", codeHash)
          .executeUpdate();
    }
}
