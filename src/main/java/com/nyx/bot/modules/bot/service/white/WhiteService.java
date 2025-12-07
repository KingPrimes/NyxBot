package com.nyx.bot.modules.bot.service.white;

import com.nyx.bot.modules.bot.entity.white.GroupWhite;
import com.nyx.bot.modules.bot.entity.white.ProveWhite;
import com.nyx.bot.modules.bot.repo.white.GroupWhiteRepository;
import com.nyx.bot.modules.bot.repo.white.ProveWhiteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class WhiteService {
    private final GroupWhiteRepository groupWhiteRepository;

    private final ProveWhiteRepository proveWhiteRepository;

    public WhiteService(GroupWhiteRepository groupWhiteRepository, ProveWhiteRepository proveWhiteRepository) {
        this.groupWhiteRepository = groupWhiteRepository;
        this.proveWhiteRepository = proveWhiteRepository;
    }

    public Page<GroupWhite> list(GroupWhite gw) {
        return groupWhiteRepository.findAllPageable(
                gw.getGroupUid(),
                PageRequest.of(
                        gw.getCurrent() - 1,
                        gw.getSize()
                )
        );
    }

    public Page<ProveWhite> list(ProveWhite pw) {
        return proveWhiteRepository.findAllPageable(
                pw.getProveUid(),
                PageRequest.of(
                        pw.getCurrent() - 1,
                        pw.getSize()
                )
        );
    }

    public GroupWhite save(GroupWhite gw) {
        groupWhiteRepository.findByGroupUid(gw.getGroupUid()).ifPresent(g -> gw.setId(g.getId()));
        return groupWhiteRepository.save(gw);
    }

    public ProveWhite save(ProveWhite pw) {
        proveWhiteRepository.findByProveUid(pw.getProveUid()).ifPresent(p -> pw.setId(p.getId()));
        return proveWhiteRepository.save(pw);
    }

    public void remove(Long id) {
        if (id != null) {
            groupWhiteRepository.deleteById(id);
        }
    }

    public void removeProve(Long id) {
        if (id != null) {
            proveWhiteRepository.deleteById(id);
        }
    }

    public boolean hasWhiteGroup() {
        return groupWhiteRepository.count() > 0;
    }

    public boolean hasWhiteProve() {
        return proveWhiteRepository.count() > 0;
    }

    public boolean isWhite(Long group, Long prove) {
        AtomicBoolean flag = new AtomicBoolean(false);
        if (group != null && group != 0L) {
            groupWhiteRepository.findByGroupUid(group).ifPresent(g -> flag.set(true));
        }
        if (prove != null && prove != 0L) {
            proveWhiteRepository.findByProveUid(prove).ifPresent(p -> flag.set(true));
        }
        return flag.get();
    }

}
