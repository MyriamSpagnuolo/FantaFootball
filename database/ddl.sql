--
-- PostgreSQL database dump
--

-- Dumped from database version 18.4 (Debian 18.4-1.pgdg13+1)
-- Dumped by pg_dump version 18.4 (Debian 18.4-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: app_user_roles; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_user_roles (
                                       user_id bigint NOT NULL,
                                       role character varying(20) NOT NULL,
                                       CONSTRAINT ck_app_user_roles_role CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'USER'::character varying])::text[])))
);


--
-- Name: seq_app_users_user_id; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.seq_app_users_user_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: app_users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_users (
                                  user_id bigint DEFAULT nextval('public.seq_app_users_user_id'::regclass) NOT NULL,
                                  username character varying(80) NOT NULL,
                                  email character varying(254) NOT NULL,
                                  password_hash character varying(120) NOT NULL,
                                  enabled boolean DEFAULT true NOT NULL,
                                  token_version integer DEFAULT 0 NOT NULL,
                                  CONSTRAINT ck_app_users_password_hash_not_blank CHECK ((btrim((password_hash)::text) <> ''::text)),
                                  CONSTRAINT ck_app_users_username_not_blank CHECK ((btrim((username)::text) <> ''::text))
);

CREATE TABLE public.password_reset_token (
    id uuid NOT NULL,
    user_id bigint NOT NULL,
    token_hash character varying(64) NOT NULL,
    created_at timestamp with time zone NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    used_at timestamp with time zone,
    CONSTRAINT ck_password_reset_token_expiry CHECK (expires_at > created_at)
);


--
-- Name: seq_league_id; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.seq_league_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: league; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.league (
                               id bigint DEFAULT nextval('public.seq_league_id'::regclass) NOT NULL,
                               name character varying(100) NOT NULL,
                               invite_code character varying(20) NOT NULL,
                               admin_user_id bigint NOT NULL,
                               creation_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                               budget integer NOT NULL
);


--
-- Name: seq_league_invite_id; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.seq_league_invite_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: league_invite; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.league_invite (
                                      id bigint DEFAULT nextval('public.seq_league_invite_id'::regclass) NOT NULL,
                                      league_id bigint NOT NULL,
                                      invited_by_user_id bigint NOT NULL,
                                      invited_user_id bigint NOT NULL,
                                      status character varying(20) NOT NULL,
                                      sent_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                                      response_date timestamp without time zone,
                                      CONSTRAINT chk_league_invite_different CHECK ((invited_by_user_id <> invited_user_id)),
                                      CONSTRAINT chk_league_invite_status CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'DECLINED'::character varying, 'EXPIRED'::character varying])::text[])))
);


--
-- Name: seq_league_match_id; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.seq_league_match_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: league_match; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.league_match (
                                     id bigint DEFAULT nextval('public.seq_league_match_id'::regclass) NOT NULL,
                                     league_id bigint NOT NULL,
                                     home_team_id bigint NOT NULL,
                                     away_team_id bigint NOT NULL,
                                     home_score numeric(5,2),
                                     away_score numeric(5,2),
                                     home_goals integer,
                                     away_goals integer,
                                     match_day timestamp without time zone NOT NULL,
                                     matchday_id bigint NOT NULL,
                                     round_number integer NOT NULL
);


--
-- Name: seq_lineup_id; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.seq_lineup_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: lineup; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.lineup (
                               id bigint DEFAULT nextval('public.seq_lineup_id'::regclass) NOT NULL,
                               team_id bigint NOT NULL,
                               league_match_id bigint NOT NULL,
                               is_defensive boolean NOT NULL,
                               lineup_type_id bigint NOT NULL
);


--
-- Name: lineup_player; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.lineup_player (
                                      lineup_id bigint NOT NULL,
                                      player_id bigint NOT NULL,
                                      starter boolean DEFAULT true NOT NULL
);


--
-- Name: lineup_type; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.lineup_type (
                                    id bigint NOT NULL,
                                    defender_num integer NOT NULL,
                                    midfielder_num integer NOT NULL,
                                    foward_num integer NOT NULL
);


--
-- Name: seq_matchday_id; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.seq_matchday_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: matchday; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.matchday (
                                 id bigint DEFAULT nextval('public.seq_matchday_id'::regclass) NOT NULL,
                                 number integer NOT NULL,
                                 date date NOT NULL,
                                 is_closed boolean DEFAULT false NOT NULL
);


--
-- Name: seq_player_id; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.seq_player_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: player; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.player (
                               id bigint DEFAULT nextval('public.seq_player_id'::regclass) NOT NULL,
                               external_id bigint NOT NULL,
                               name character varying NOT NULL,
                               surname character varying NOT NULL,
                               real_team_name character varying NOT NULL,
                               real_team_shirt_num integer NOT NULL,
                               price integer NOT NULL,
                               is_injured boolean NOT NULL,
                               "position" character varying(1) NOT NULL,
                               CONSTRAINT ck_player_position CHECK ((("position")::text = ANY ((ARRAY['P'::character varying, 'D'::character varying, 'C'::character varying, 'A'::character varying])::text[])))
);


--
-- Name: seq_player_results_id; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.seq_player_results_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: player_results; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.player_results (
                                       id bigint DEFAULT nextval('public.seq_player_results_id'::regclass) NOT NULL,
                                       player_id bigint NOT NULL,
                                       rating numeric,
                                       goal_num integer NOT NULL,
                                       goal_conceded integer NOT NULL,
                                       autogoal_num integer NOT NULL,
                                       assist_num integer NOT NULL,
                                       penalty_saved integer NOT NULL,
                                       penalty_failed integer NOT NULL,
                                       clean_sheet boolean,
                                       yellow_card integer NOT NULL,
                                       red_card boolean NOT NULL,
                                       matchday_id bigint NOT NULL
);


--
-- Name: seq_team_id; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.seq_team_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: seq_team_player_id; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.seq_team_player_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: seq_trade_id; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.seq_trade_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: team; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.team (
                             id bigint DEFAULT nextval('public.seq_team_id'::regclass) NOT NULL,
                             name character varying(100) NOT NULL,
                             user_id bigint NOT NULL,
                             league_id bigint NOT NULL,
                             budget integer DEFAULT 0 NOT NULL,
                             total_points integer DEFAULT 0 NOT NULL,
                             CONSTRAINT chk_budget CHECK ((budget >= 0)),
                             CONSTRAINT chk_points CHECK ((total_points >= 0))
);


--
-- Name: team_player; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.team_player (
                                    id bigint DEFAULT nextval('public.seq_team_player_id'::regclass) NOT NULL,
                                    team_id bigint NOT NULL,
                                    league_id bigint NOT NULL,
                                    player_id bigint NOT NULL,
                                    purchase_date date NOT NULL,
                                    transfer_date date,
                                    purchase_price integer NOT NULL,
                                    CONSTRAINT chk_purchase_price CHECK ((purchase_price >= 0))
);


--
-- Name: trade; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.trade (
                              id bigint DEFAULT nextval('public.seq_trade_id'::regclass) NOT NULL,
                              proposing_team_id bigint NOT NULL,
                              status character varying(20) NOT NULL,
                              proposal_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
                              trade_player_id bigint NOT NULL,
                              receiving_team_id bigint NOT NULL,
                              offered_player_id bigint NOT NULL,
                              amount integer,
                              CONSTRAINT chk_trade_different CHECK ((proposing_team_id <> receiving_team_id)),
                              CONSTRAINT chk_trade_players_different CHECK ((offered_player_id <> trade_player_id)),
                              CONSTRAINT chk_trade_status CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'CANCELLED'::character varying])::text[])))
);


--
-- Name: app_users app_users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_users
    ADD CONSTRAINT app_users_pkey PRIMARY KEY (user_id);


--
-- Name: league_match league_match_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.league_match
    ADD CONSTRAINT league_match_pkey PRIMARY KEY (id);


--
-- Name: league league_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.league
    ADD CONSTRAINT league_pkey PRIMARY KEY (id);


--
-- Name: league_invite league_invite_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.league_invite
    ADD CONSTRAINT league_invite_pkey PRIMARY KEY (id);


--
-- Name: lineup lineup_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lineup
    ADD CONSTRAINT lineup_pkey PRIMARY KEY (id);


--
-- Name: lineup_player lineup_player_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lineup_player
    ADD CONSTRAINT lineup_player_pkey PRIMARY KEY (lineup_id, player_id);


--
-- Name: lineup_type lineup_type_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lineup_type
    ADD CONSTRAINT lineup_type_pkey PRIMARY KEY (id);


--
-- Name: matchday matchday_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.matchday
    ADD CONSTRAINT matchday_pkey PRIMARY KEY (id);


--
-- Name: app_user_roles pk_app_user_roles; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user_roles
    ADD CONSTRAINT pk_app_user_roles PRIMARY KEY (user_id, role);


--
-- Name: player player_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.player
    ADD CONSTRAINT player_pkey PRIMARY KEY (id);


--
-- Name: player_results player_results_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.player_results
    ADD CONSTRAINT player_results_pkey PRIMARY KEY (id);


--
-- Name: team team_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.team
    ADD CONSTRAINT team_pkey PRIMARY KEY (id);


--
-- Name: team_player team_player_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.team_player
    ADD CONSTRAINT team_player_pkey PRIMARY KEY (id);


--
-- Name: trade trade_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trade
    ADD CONSTRAINT trade_pkey PRIMARY KEY (id);


--
-- Name: app_users uq_app_users_username; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_users
    ADD CONSTRAINT uq_app_users_username UNIQUE (username);

ALTER TABLE ONLY public.password_reset_token
    ADD CONSTRAINT password_reset_token_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.password_reset_token
    ADD CONSTRAINT uq_password_reset_token_hash UNIQUE (token_hash);


--
-- Name: league uq_league_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.league
    ADD CONSTRAINT uq_league_code UNIQUE (invite_code);


--
-- Name: lineup uq_lineup; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lineup
    ADD CONSTRAINT uq_lineup UNIQUE (team_id, league_match_id);


--
-- Name: matchday uq_matchday_number; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.matchday
    ADD CONSTRAINT uq_matchday_number UNIQUE (number);


--
-- Name: player uq_player_external_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.player
    ADD CONSTRAINT uq_player_external_id UNIQUE (external_id);


--
-- Name: player_results uq_player_results_player_matchday; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.player_results
    ADD CONSTRAINT uq_player_results_player_matchday UNIQUE (player_id, matchday_id);


--
-- Name: team uq_team_name_league; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.team
    ADD CONSTRAINT uq_team_name_league UNIQUE (name, league_id);


--
-- Name: team uq_team_user_league; Type: CONSTRAINT; Schema: public; Owner: -
--
-- Un utente puo' avere al piu' un team per lega (ma team in leghe diverse).

ALTER TABLE ONLY public.team
    ADD CONSTRAINT uq_team_user_league UNIQUE (user_id, league_id);


--
-- Name: ux_app_users_username_lower; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX ux_app_users_username_lower ON public.app_users USING btree (lower((username)::text));

CREATE UNIQUE INDEX ux_app_users_email_lower ON public.app_users USING btree (lower((email)::text));
CREATE INDEX ix_password_reset_token_user_id ON public.password_reset_token USING btree (user_id);
CREATE INDEX ix_password_reset_token_expires_at ON public.password_reset_token USING btree (expires_at);


--
-- Name: ux_team_player_active_per_league; Type: INDEX; Schema: public; Owner: -
--
-- Un giocatore puo' avere al piu' un possesso attivo (transfer_date IS NULL)
-- per lega; leghe diverse possono possedere lo stesso player in parallelo.

CREATE UNIQUE INDEX ux_team_player_active_per_league ON public.team_player USING btree (player_id, league_id) WHERE (transfer_date IS NULL);


--
-- Name: ux_league_invite_pending_unique; Type: INDEX; Schema: public; Owner: -
--
-- Al piu' un invito PENDING per (lega, utente invitato): evita inviti duplicati.

CREATE UNIQUE INDEX ux_league_invite_pending_unique ON public.league_invite USING btree (league_id, invited_user_id) WHERE ((status)::text = 'PENDING'::text);


--
-- Name: app_user_roles fk_app_user_roles_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user_roles
    ADD CONSTRAINT fk_app_user_roles_user FOREIGN KEY (user_id) REFERENCES public.app_users(user_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY public.password_reset_token
    ADD CONSTRAINT fk_password_reset_token_user FOREIGN KEY (user_id) REFERENCES public.app_users(user_id) ON DELETE CASCADE;


--
-- Name: league fk_league_admin; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.league
    ADD CONSTRAINT fk_league_admin FOREIGN KEY (admin_user_id) REFERENCES public.app_users(user_id) ON DELETE RESTRICT;


--
-- Name: league_invite fk_li_invited_by; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.league_invite
    ADD CONSTRAINT fk_li_invited_by FOREIGN KEY (invited_by_user_id) REFERENCES public.app_users(user_id) ON DELETE RESTRICT;


--
-- Name: league_invite fk_li_invited_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.league_invite
    ADD CONSTRAINT fk_li_invited_user FOREIGN KEY (invited_user_id) REFERENCES public.app_users(user_id) ON DELETE CASCADE;


--
-- Name: league_invite fk_li_league; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.league_invite
    ADD CONSTRAINT fk_li_league FOREIGN KEY (league_id) REFERENCES public.league(id) ON DELETE CASCADE;


--
-- Name: lineup fk_lineup_leaguematch; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lineup
    ADD CONSTRAINT fk_lineup_leaguematch FOREIGN KEY (league_match_id) REFERENCES public.league_match(id) NOT VALID;


--
-- Name: lineup fk_lineup_lineuptype; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lineup
    ADD CONSTRAINT fk_lineup_lineuptype FOREIGN KEY (lineup_type_id) REFERENCES public.lineup_type(id) NOT VALID;


--
-- Name: lineup fk_lineup_team; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lineup
    ADD CONSTRAINT fk_lineup_team FOREIGN KEY (team_id) REFERENCES public.team(id) ON DELETE CASCADE;


--
-- Name: league_match fk_lm_away; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.league_match
    ADD CONSTRAINT fk_lm_away FOREIGN KEY (away_team_id) REFERENCES public.team(id);


--
-- Name: league_match fk_lm_home; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.league_match
    ADD CONSTRAINT fk_lm_home FOREIGN KEY (home_team_id) REFERENCES public.team(id);


--
-- Name: league_match fk_lm_league; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.league_match
    ADD CONSTRAINT fk_lm_league FOREIGN KEY (league_id) REFERENCES public.league(id);


--
-- Name: league_match fk_lm_matchday; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.league_match
    ADD CONSTRAINT fk_lm_matchday FOREIGN KEY (matchday_id) REFERENCES public.matchday(id);


--
-- Name: lineup_player fk_lp_lineup; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lineup_player
    ADD CONSTRAINT fk_lp_lineup FOREIGN KEY (lineup_id) REFERENCES public.lineup(id) ON DELETE CASCADE;


--
-- Name: lineup_player fk_lp_teamplayer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.lineup_player
    ADD CONSTRAINT fk_lp_teamplayer FOREIGN KEY (player_id) REFERENCES public.team_player(id);


--
-- Name: player_results fk_player_results_matchday; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.player_results
    ADD CONSTRAINT fk_player_results_matchday FOREIGN KEY (matchday_id) REFERENCES public.matchday(id);


--
-- Name: player_results fk_player_results_player; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.player_results
    ADD CONSTRAINT fk_player_results_player FOREIGN KEY (player_id) REFERENCES public.player(id);


--
-- Name: team fk_team_league; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.team
    ADD CONSTRAINT fk_team_league FOREIGN KEY (league_id) REFERENCES public.league(id) ON DELETE CASCADE;


--
-- Name: team fk_team_user; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.team
    ADD CONSTRAINT fk_team_user FOREIGN KEY (user_id) REFERENCES public.app_users(user_id) ON DELETE CASCADE;


--
-- Name: team_player fk_tp_league; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.team_player
    ADD CONSTRAINT fk_tp_league FOREIGN KEY (league_id) REFERENCES public.league(id) ON DELETE CASCADE;


--
-- Name: team_player fk_tp_player; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.team_player
    ADD CONSTRAINT fk_tp_player FOREIGN KEY (player_id) REFERENCES public.player(id);


--
-- Name: team_player fk_tp_team; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.team_player
    ADD CONSTRAINT fk_tp_team FOREIGN KEY (team_id) REFERENCES public.team(id) ON DELETE CASCADE;


--
-- Name: trade fk_trade_offered_player; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trade
    ADD CONSTRAINT fk_trade_offered_player FOREIGN KEY (offered_player_id) REFERENCES public.team_player(id);


--
-- Name: trade fk_trade_proposing; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trade
    ADD CONSTRAINT fk_trade_proposing FOREIGN KEY (proposing_team_id) REFERENCES public.team(id) ON DELETE CASCADE;


--
-- Name: trade fk_trade_receiving; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trade
    ADD CONSTRAINT fk_trade_receiving FOREIGN KEY (receiving_team_id) REFERENCES public.team(id) ON DELETE CASCADE;


--
-- Name: trade fk_trade_teamplayer; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.trade
    ADD CONSTRAINT fk_trade_teamplayer FOREIGN KEY (trade_player_id) REFERENCES public.team_player(id) NOT VALID;


--
-- PostgreSQL database dump complete
--
