package com.nyx.bot.repo.impl.black;

import com.nyx.bot.entity.bot.black.GroupBlack;
import com.nyx.bot.entity.bot.black.ProveBlack;
import com.nyx.bot.repo.bot.black.GroupBlackRepository;
import com.nyx.bot.repo.bot.black.ProveBlackRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
public class BlackService {

    @Resource
    GroupBlackRepository repository;

    @Resource
    ProveBlackRepository proveBlackRepository;


    public GroupBlack findByGroupId(Long id) {
        return repository.findByGroupUid(id);
    }

    public ProveBlack findByProveId(Long id) {
        return proveBlackRepository.findByProve(id);
    }


    public Page<GroupBlack> list(GroupBlack gb) {
        return repository.findAllPageable(
                gb.getGroupUid(),
                PageRequest.of(
                        gb.getPageNum() - 1,
                        gb.getPageSize()
                )
        );
    }

    public int save(GroupBlack gb) {
        GroupBlack groupBlack = repository.findByGroupUid(gb.getGroupUid());
        if (groupBlack != null) {
            gb.setId(groupBlack.getGroupUid());
        }
        repository.save(gb);
        return 1;
    }

    public int remove(Long id) {
        if (id != null) {
            repository.deleteById(id);
            return 1;
        } else {
            return 0;
        }
    }


    public Page<ProveBlack> list(ProveBlack pb) {
        return proveBlackRepository.findAllPageable(
                pb.getProve(),
                PageRequest.of(
                        pb.getPageNum() - 1,
                        pb.getPageSize()
                )
        );
    }

    public int save(ProveBlack pb) {
        ProveBlack prove = proveBlackRepository.findByProve(pb.getProve());
        if (prove != null) {
            pb.setId(prove.getId());
        }
        proveBlackRepository.save(pb);
        return 1;
    }

    public int removeProve(Long id) {
        if (id != null) {
            proveBlackRepository.deleteById(id);
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 判断是否是黑名单
     *
     * @param groupUid 群号
     * @param userUid  qq号
     * @return false黑名单
     */
    public boolean isBlack(Long groupUid, Long userUid) {
        AtomicBoolean flag = new AtomicBoolean(true);
        if (groupUid != null && groupUid != 0L) {
            GroupBlack byGroupUid = repository.findByGroupUid(groupUid);
            Optional.ofNullable(byGroupUid).ifPresent(g -> {
                flag.set(false);
            });
        }
        ProveBlack byUserUid = proveBlackRepository.findByProve(userUid);
        Optional.ofNullable(byUserUid).ifPresent(p -> {
            flag.set(false);
        });
        return flag.get();
    }

}
