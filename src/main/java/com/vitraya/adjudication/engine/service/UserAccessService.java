package com.vitraya.adjudication.engine.service;

import com.vitraya.adjudication.engine.mysql.entity.UserAccess;
import com.vitraya.adjudication.engine.mysql.repository.UserAccessRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserAccessService {

    @Value("${user.access.control.enabled}")
    private boolean userAccessControlEnabled;

    private final UserAccessRepository userAccessRepository;

    public UserAccessService(UserAccessRepository userAccessRepository) {
        this.userAccessRepository = userAccessRepository;
    }

    public boolean isAccessAllowed(String username, String endpoint, String method) {
        if (userAccessControlEnabled) {
            long userid = 1L;
            List<UserAccess> userAccessList = userAccessRepository.findByUserId(userid);

            for (UserAccess access : userAccessList) {
                switch (method) {
                    case "POST":
                        if (access.getCanCreate()) return true;
                        break;
                    case "GET":
                        if (access.getCanRead()) return true;
                        break;
                    case "PUT":
                        if (access.getCanUpdate()) return true;
                        break;
                    case "DELETE":
                        if (access.getCanDelete()) return true;
                        break;
                }
            }
        } else {
            return true;
        }

        return false;
    }
}
