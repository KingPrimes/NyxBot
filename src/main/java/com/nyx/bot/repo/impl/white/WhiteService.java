package com.nyx.bot.repo.impl.white;

import com.nyx.bot.entity.bot.white.GroupWhite;
import com.nyx.bot.entity.bot.white.ProveWhite;
import com.nyx.bot.repo.bot.white.GroupWhiteRepository;
import com.nyx.bot.repo.bot.white.ProveWhiteRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class WhiteService {

    @Resource
    GroupWhiteRepository groupWhiteRepository;

    @Resource
    ProveWhiteRepository proveWhiteRepository;


    public GroupWhite findByGroup(Long group) {
        return groupWhiteRepository.findByGroupUid(group).orElse(new GroupWhite());
    }

    public ProveWhite findByProve(Long prove) {
        return proveWhiteRepository.findByProve(prove).orElse(new ProveWhite());
    }

    public Page<GroupWhite> list(GroupWhite gw) {
        return groupWhiteRepository.findAllPageable(
                gw.getGroupUid(),
                PageRequest.of(
                        gw.getPageNum() - 1,
                        gw.getPageSize()
                )
        );
    }

    public Page<ProveWhite> list(ProveWhite pw) {
        return proveWhiteRepository.findAllPageable(
                pw.getProve(),
                PageRequest.of(
                        pw.getPageNum() - 1,
                        pw.getPageSize()
                )
        );
    }

    public GroupWhite save(GroupWhite gw) {
        groupWhiteRepository.findByGroupUid(gw.getGroupUid()).ifPresent(g -> gw.setId(g.getId()));
        return groupWhiteRepository.save(gw);
    }

    public ProveWhite save(ProveWhite pw) {
        proveWhiteRepository.findByProve(pw.getProve()).ifPresent(p -> pw.setId(p.getId()));
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

    public boolean isWhite(Long group, Long prove) {
        AtomicBoolean flag = new AtomicBoolean(false);
        if (group != null && group != 0L) {
            groupWhiteRepository.findByGroupUid(group).ifPresent(g -> flag.set(true));
        }
        proveWhiteRepository.findByProve(prove).ifPresent(p -> flag.set(true));
        return flag.get();
    }

}
