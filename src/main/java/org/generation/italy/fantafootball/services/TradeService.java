package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.TradeDto;
import org.generation.italy.fantafootball.model.entities.TradeStatus;
import org.generation.italy.fantafootball.model.repositories.TradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeService {
    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository){
        this.tradeRepository = tradeRepository;
    }

    public List<TradeDto> getAllByUserId(Long id){
        return tradeRepository.findAllByUserId(id).stream()
                .map(TradeDto::fromEntity)
                .toList();
    }

    public List<TradeDto> getAllPendingSentTradesByTeamId(Long id){
        return tradeRepository.findByProposingTeam_IdAndStatus(id, TradeStatus.PENDING).stream()
                .map(TradeDto::fromEntity)
                .toList();
    }

    public List<TradeDto> getAllPendingReceivedTradesByTeamId(Long id){
        return tradeRepository.findByReceivingTeam_IdAndStatus(id, TradeStatus.PENDING).stream()
                .map(TradeDto::fromEntity)
                .toList();
    }

    public List<TradeDto> getTradeHistoryByTeamId(Long id){
        return tradeRepository.findTradeHistoryByTeamId(id).stream()
                .map(TradeDto::fromEntity)
                .toList();
    }

    public void deleteTradeById(Long id){
        tradeRepository.deleteById(id);
    }
}
