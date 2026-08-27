package org.generation.italy.fantafootball.services;

import org.generation.italy.fantafootball.model.dto.TradeDto;
import org.generation.italy.fantafootball.model.repositories.TradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeService {
    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository){
        this.tradeRepository = tradeRepository;
    }

    public List<TradeDto> getAllSentTradesByTeamId(Long id){
        return tradeRepository.findByProposingTeam_Id(id).stream()
                .map(x -> TradeDto.fromEntity(x, x.getReceivingTeam().getName()))
                .toList();
    }

    public List<TradeDto> getAllReceivedTradesByTeamId(Long id){
        return tradeRepository.findByReceivingTeam_Id(id).stream()
                .map(x -> TradeDto.fromEntity(x, x.getProposingTeam().getName()))
                .toList();
    }


    public void deleteTradeById(Long id){

        tradeRepository.deleteById(id);
    }
}
