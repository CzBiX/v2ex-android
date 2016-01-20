package com.czbix.v2ex.model.json;

import com.czbix.v2ex.model.Avatar;
import com.czbix.v2ex.model.Member;

public class MemberBean {
    public String mUsername;
    public String mAvatarMini;

    public Member toModel() {
        final Avatar avatar = new Avatar.Builder().setUrl(mAvatarMini).createAvatar();

        return new Member.Builder().setUsername(mUsername).setAvatar(avatar).createMember();
    }
}
