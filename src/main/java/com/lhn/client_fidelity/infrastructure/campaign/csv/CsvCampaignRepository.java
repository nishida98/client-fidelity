package com.lhn.client_fidelity.infrastructure.campaign.csv;

import com.lhn.client_fidelity.application.campaign.CampaignRepository;
import com.lhn.client_fidelity.domain.campaign.Campaign;
import com.lhn.client_fidelity.domain.campaign.CampaignId;
import com.lhn.client_fidelity.domain.user.UserId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(name = "client-fidelity.persistence.type", havingValue = "csv")
public class CsvCampaignRepository implements CampaignRepository {

	private static final String HEADER = "id,commerce_id,name,points,start_date,expiration_date,updated_at,template_payload";

	private final Path path;

	public CsvCampaignRepository(@Value("${client-fidelity.persistence.csv.campaign-path}") String path) {
		this.path = Path.of(path);
	}

	public CsvCampaignRepository(Path path) {
		this.path = path;
	}

	@Override
	public synchronized Campaign save(Campaign campaign) {
		List<CsvCampaignRecord> records = records();
		CsvCampaignRecord updatedRecord = CsvCampaignRecord.from(campaign);
		boolean replaced = false;
		for (int index = 0; index < records.size(); index++) {
			CsvCampaignRecord record = records.get(index);
			if (record.id().equals(campaign.id().value())) {
				records.set(index, updatedRecord);
				replaced = true;
				break;
			}
		}
		if (!replaced) {
			records.add(updatedRecord);
		}
		writeRecords(records);
		return campaign;
	}

	@Override
	public synchronized Optional<Campaign> findByIdAndCommerceId(CampaignId campaignId, UserId commerceId) {
		return records().stream()
				.filter(record -> record.id().equals(campaignId.value()) && record.commerceId().equals(commerceId.value()))
				.findFirst()
				.map(CsvCampaignRecord::toDomain);
	}

	@Override
	public synchronized List<Campaign> findAllByCommerceId(UserId commerceId) {
		return records().stream()
				.filter(record -> record.commerceId().equals(commerceId.value()))
				.map(CsvCampaignRecord::toDomain)
				.toList();
	}

	@Override
	public synchronized boolean deleteByIdAndCommerceId(CampaignId campaignId, UserId commerceId) {
		List<CsvCampaignRecord> records = records();
		boolean removed = records.removeIf(record ->
				record.id().equals(campaignId.value()) && record.commerceId().equals(commerceId.value())
		);
		if (removed) {
			writeRecords(records);
		}
		return removed;
	}

	private List<CsvCampaignRecord> records() {
		try {
			ensureFileExists();
			List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
			List<CsvCampaignRecord> records = new ArrayList<>();
			for (int index = 1; index < lines.size(); index++) {
				if (!lines.get(index).isBlank()) {
					records.add(CsvCampaignRecord.fromLine(lines.get(index)));
				}
			}
			return records;
		}
		catch (IOException exception) {
			throw new IllegalStateException("Could not read campaign CSV file.", exception);
		}
	}

	private void writeRecords(List<CsvCampaignRecord> records) {
		try {
			ensureFileExists();
			List<String> lines = new ArrayList<>();
			lines.add(HEADER);
			for (CsvCampaignRecord record : records) {
				lines.add(record.toLine());
			}
			Files.write(path, lines, StandardCharsets.UTF_8);
		}
		catch (IOException exception) {
			throw new IllegalStateException("Could not write campaign CSV file.", exception);
		}
	}

	private void ensureFileExists() throws IOException {
		Path parent = path.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		if (Files.notExists(path)) {
			Files.writeString(path, HEADER + System.lineSeparator(), StandardCharsets.UTF_8, StandardOpenOption.CREATE);
		}
	}
}
